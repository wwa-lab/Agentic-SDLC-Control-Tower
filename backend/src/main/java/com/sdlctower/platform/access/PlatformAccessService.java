package com.sdlctower.platform.access;

import com.sdlctower.platform.audit.AuditEvent;
import com.sdlctower.platform.audit.AuditWriter;
import com.sdlctower.platform.auth.CurrentUserDto;
import com.sdlctower.platform.auth.ScopeDto;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformAccessService {

    private static final Set<String> ROLES = Set.of(
            "PLATFORM_ADMIN",
            "WORKSPACE_ADMIN",
            "WORKSPACE_MEMBER",
            "WORKSPACE_VIEWER",
            "AUDITOR"
    );
    private static final Set<String> SCOPE_TYPES = Set.of("platform", "application", "snow_group", "workspace", "project");

    private final PlatformUserRepository users;
    private final RoleAssignmentRepository assignments;
    private final AuditWriter auditWriter;

    public PlatformAccessService(PlatformUserRepository users, RoleAssignmentRepository assignments, AuditWriter auditWriter) {
        this.users = users;
        this.assignments = assignments;
        this.auditWriter = auditWriter;
    }

    @Transactional(readOnly = true)
    public boolean isActiveUser(String staffId) {
        return users.findById(staffId)
                .map(user -> "active".equalsIgnoreCase(user.getStatus()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public CurrentUserDto currentUser(String staffId, String authProvider) {
        PlatformUserEntity user = users.findById(staffId)
                .filter(item -> "active".equalsIgnoreCase(item.getStatus()))
                .orElseThrow(() -> new IllegalArgumentException("User is not provisioned or active"));
        List<RoleAssignmentDto> grants = assignments.search(staffId, null, null, null)
                .stream()
                .map(this::toDto)
                .toList();
        List<String> roles = grants.stream().map(RoleAssignmentDto::role).distinct().toList();
        List<ScopeDto> scopes = grants.stream()
                .map(item -> new ScopeDto(item.scopeType(), item.scopeId()))
                .distinct()
                .toList();
        return new CurrentUserDto(
                "staff",
                authProvider,
                staffId,
                user.getDisplayName(),
                user.getStaffName(),
                user.getAvatarUrl(),
                roles,
                false,
                scopes
        );
    }

    @Transactional(readOnly = true)
    public List<PlatformUserDto> listUsers(String status, String profileSource, String q) {
        return users.search(blankToNull(status), blankToNull(profileSource), blankToNull(q))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PlatformUserDto createUser(UpsertPlatformUserRequest request, String actorStaffId) {
        validateStaffId(request.staffId());
        if (users.existsById(request.staffId())) {
            throw new PlatformAccessException("USER_ALREADY_EXISTS", HttpStatus.CONFLICT);
        }
        return upsertUser(request, actorStaffId);
    }

    @Transactional
    public PlatformUserDto upsertUser(UpsertPlatformUserRequest request) {
        return upsertUser(request, "system");
    }

    @Transactional
    public PlatformUserDto updateUser(UpsertPlatformUserRequest request, String actorStaffId) {
        validateStaffId(request.staffId());
        if (!users.existsById(request.staffId())) {
            throw new PlatformAccessException("USER_NOT_FOUND", HttpStatus.NOT_FOUND);
        }
        return upsertUser(request, actorStaffId);
    }

    @Transactional
    public PlatformUserDto upsertUser(UpsertPlatformUserRequest request, String actorStaffId) {
        validateStaffId(request.staffId());
        Instant now = Instant.now();
        PlatformUserEntity existing = users.findById(request.staffId()).orElse(null);
        PlatformUserDto before = existing == null ? null : toDto(existing);
        String nextStatus = defaultString(request.status(), existing == null ? "active" : existing.getStatus()).toLowerCase();
        if (!Set.of("active", "inactive").contains(nextStatus)) {
            throw new PlatformAccessException("INVALID_USER_STATUS", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (existing != null && "active".equalsIgnoreCase(existing.getStatus()) && "inactive".equals(nextStatus)) {
            assertNotLastPlatformAdminUser(existing.getStaffId());
        }

        PlatformUserEntity next = existing == null ? new PlatformUserEntity() : existing;
        if (existing == null) {
            next.setStaffId(request.staffId());
            next.setCreatedAt(now);
        }
        next.setDisplayName(defaultString(request.displayName(), request.staffId()));
        next.setStaffName(blankToNull(request.staffName()));
        next.setAvatarUrl(blankToNull(request.avatarUrl()));
        next.setEmail(blankToNull(request.email()));
        next.setProfileSource(defaultString(request.profileSource(), "manual"));
        if ("teambook".equals(next.getProfileSource())) {
            next.setLastProfileSyncAt(now);
        }
        next.setStatus(nextStatus);
        next.setUpdatedAt(now);
        PlatformUserDto saved = toDto(users.save(next));

        writeAccessAudit(
                actorStaffId,
                existing == null ? "user.create" : "inactive".equals(saved.status()) && !"inactive".equalsIgnoreCase(before.status()) ? "user.deactivate" : "user.update",
                "platform_user",
                saved.staffId(),
                "platform",
                "*",
                before,
                saved,
                request
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public List<RoleAssignmentDto> listAssignments(String staffId, String role, String scopeType, String scopeId) {
        return assignments.search(blankToNull(staffId), blankToNull(role), blankToNull(scopeType), blankToNull(scopeId))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public RoleAssignmentDto assignRole(AssignRoleRequest request, String grantedBy) {
        validateAssignment(request);
        PlatformUserEntity user = users.findById(request.staffId())
                .orElseThrow(() -> new PlatformAccessException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));
        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new PlatformAccessException("USER_INACTIVE", HttpStatus.CONFLICT);
        }
        assignments.findByStaffIdAndRoleAndScopeTypeAndScopeId(request.staffId(), request.role(), request.scopeType(), request.scopeId())
                .ifPresent(existing -> {
                    throw new PlatformAccessException("ROLE_ASSIGNMENT_EXISTS", HttpStatus.CONFLICT);
                });
        RoleAssignmentEntity entity = new RoleAssignmentEntity();
        entity.setId(assignmentId(request));
        entity.setStaffId(request.staffId());
        entity.setUserDisplayName(user.getDisplayName());
        entity.setRole(request.role());
        entity.setScopeType(request.scopeType());
        entity.setScopeId(request.scopeId());
        entity.setGrantedBy(defaultString(grantedBy, "system"));
        entity.setGrantedAt(Instant.now());
        RoleAssignmentDto saved = toDto(assignments.save(entity));
        writeAccessAudit(
                grantedBy,
                "role.grant",
                "role_assignment",
                saved.id(),
                saved.scopeType(),
                saved.scopeId(),
                null,
                saved,
                request
        );
        return saved;
    }

    @Transactional
    public void revoke(String assignmentId) {
        revoke(assignmentId, "system");
    }

    @Transactional
    public void revoke(String assignmentId, String actorStaffId) {
        RoleAssignmentEntity target = assignments.findById(assignmentId)
                .orElseThrow(() -> new PlatformAccessException("ROLE_ASSIGNMENT_NOT_FOUND", HttpStatus.NOT_FOUND));
        if ("PLATFORM_ADMIN".equals(target.getRole())) {
            assertMoreThanOneActivePlatformAdmin();
        }
        RoleAssignmentDto before = toDto(target);
        assignments.delete(target);
        writeAccessAudit(
                actorStaffId,
                "role.revoke",
                "role_assignment",
                before.id(),
                before.scopeType(),
                before.scopeId(),
                before,
                null,
                Map.of("assignmentId", assignmentId)
        );
    }

    private void validateAssignment(AssignRoleRequest request) {
        validateStaffId(request.staffId());
        if (!ROLES.contains(request.role())) {
            throw new PlatformAccessException("INVALID_ROLE", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!SCOPE_TYPES.contains(request.scopeType())) {
            throw new PlatformAccessException("INVALID_SCOPE", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if ("platform".equals(request.scopeType()) && !"*".equals(request.scopeId())) {
            throw new PlatformAccessException("INVALID_SCOPE", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!"platform".equals(request.scopeType()) && blankToNull(request.scopeId()) == null) {
            throw new PlatformAccessException("INVALID_SCOPE", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void validateStaffId(String staffId) {
        if (blankToNull(staffId) == null) {
            throw new PlatformAccessException("USER_NOT_FOUND", HttpStatus.NOT_FOUND);
        }
    }

    private void assertNotLastPlatformAdminUser(String staffId) {
        boolean hasAdminGrant = assignments.findByStaffIdAndRoleAndScopeTypeAndScopeId(staffId, "PLATFORM_ADMIN", "platform", "*").isPresent();
        if (hasAdminGrant) {
            assertMoreThanOneActivePlatformAdmin();
        }
    }

    private void assertMoreThanOneActivePlatformAdmin() {
        if (assignments.countActivePlatformAdmins() <= 1) {
            throw new PlatformAccessException("LAST_PLATFORM_ADMIN", HttpStatus.CONFLICT);
        }
    }

    private void writeAccessAudit(
            String actorStaffId,
            String action,
            String objectType,
            String objectId,
            String scopeType,
            String scopeId,
            Object before,
            Object after,
            Object request
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", before);
        payload.put("after", after);
        payload.put("request", request);
        payload.put("source", "platform-access");
        String actor = defaultString(actorStaffId, "system");
        auditWriter.write(new AuditEvent(
                actor,
                "system".equals(actor) ? "system" : "user",
                "permission_change",
                action,
                objectType,
                objectId,
                scopeType,
                scopeId,
                "success",
                null,
                payload
        ));
    }

    private PlatformUserDto toDto(PlatformUserEntity entity) {
        return new PlatformUserDto(
                entity.getStaffId(),
                entity.getDisplayName(),
                entity.getStaffName(),
                entity.getAvatarUrl(),
                entity.getEmail(),
                entity.getProfileSource(),
                entity.getLastProfileSyncAt(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private RoleAssignmentDto toDto(RoleAssignmentEntity entity) {
        return new RoleAssignmentDto(
                entity.getId(),
                entity.getStaffId(),
                entity.getUserDisplayName(),
                entity.getRole(),
                entity.getScopeType(),
                entity.getScopeId(),
                entity.getGrantedBy(),
                entity.getGrantedAt()
        );
    }

    private String assignmentId(AssignRoleRequest request) {
        return "ra-" + request.staffId() + "-" + request.role().toLowerCase() + "-" + request.scopeType() + "-" + request.scopeId().replace("*", "platform");
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
