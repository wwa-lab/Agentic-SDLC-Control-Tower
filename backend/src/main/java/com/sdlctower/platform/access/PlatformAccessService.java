package com.sdlctower.platform.access;

import com.sdlctower.platform.auth.CurrentUserDto;
import com.sdlctower.platform.auth.ScopeDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class PlatformAccessService {

    private final Map<String, PlatformUserDto> users = new ConcurrentHashMap<>();
    private final Map<String, RoleAssignmentDto> assignments = new ConcurrentHashMap<>();

    public PlatformAccessService() {
        seed();
    }

    public boolean isActiveUser(String staffId) {
        PlatformUserDto user = users.get(staffId);
        return user != null && "active".equalsIgnoreCase(user.status());
    }

    public CurrentUserDto currentUser(String staffId, String authProvider) {
        PlatformUserDto user = users.get(staffId);
        if (user == null || !"active".equalsIgnoreCase(user.status())) {
            throw new IllegalArgumentException("User is not provisioned or active");
        }
        List<RoleAssignmentDto> grants = assignments.values().stream()
                .filter(item -> item.staffId().equals(staffId))
                .sorted(Comparator.comparing(RoleAssignmentDto::role).thenComparing(RoleAssignmentDto::scopeType))
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
                user.displayName(),
                user.staffName(),
                user.avatarUrl(),
                roles,
                false,
                scopes
        );
    }

    public List<PlatformUserDto> listUsers(String status, String profileSource, String q) {
        String normalizedQ = q == null ? "" : q.toLowerCase();
        return users.values().stream()
                .filter(user -> status == null || status.isBlank() || user.status().equalsIgnoreCase(status))
                .filter(user -> profileSource == null || profileSource.isBlank() || user.profileSource().equalsIgnoreCase(profileSource))
                .filter(user -> normalizedQ.isBlank()
                        || user.staffId().toLowerCase().contains(normalizedQ)
                        || user.displayName().toLowerCase().contains(normalizedQ)
                        || (user.staffName() != null && user.staffName().toLowerCase().contains(normalizedQ))
                        || (user.email() != null && user.email().toLowerCase().contains(normalizedQ)))
                .sorted(Comparator.comparing(PlatformUserDto::staffId))
                .toList();
    }

    public PlatformUserDto upsertUser(UpsertPlatformUserRequest request) {
        Instant now = Instant.now();
        PlatformUserDto existing = users.get(request.staffId());
        PlatformUserDto next = new PlatformUserDto(
                request.staffId(),
                defaultString(request.displayName(), request.staffId()),
                blankToNull(request.staffName()),
                blankToNull(request.avatarUrl()),
                blankToNull(request.email()),
                defaultString(request.profileSource(), "manual"),
                existing != null && "teambook".equals(request.profileSource()) ? now : existing == null ? null : existing.lastProfileSyncAt(),
                defaultString(request.status(), "active"),
                existing == null ? now : existing.createdAt(),
                now
        );
        users.put(next.staffId(), next);
        return next;
    }

    public List<RoleAssignmentDto> listAssignments(String staffId, String role, String scopeType, String scopeId) {
        return assignments.values().stream()
                .filter(item -> staffId == null || staffId.isBlank() || item.staffId().equals(staffId))
                .filter(item -> role == null || role.isBlank() || item.role().equals(role))
                .filter(item -> scopeType == null || scopeType.isBlank() || item.scopeType().equals(scopeType))
                .filter(item -> scopeId == null || scopeId.isBlank() || item.scopeId().equals(scopeId))
                .sorted(Comparator.comparing(RoleAssignmentDto::grantedAt).reversed())
                .toList();
    }

    public RoleAssignmentDto assignRole(AssignRoleRequest request, String grantedBy) {
        if (!isActiveUser(request.staffId())) {
            throw new IllegalArgumentException("Cannot assign role to inactive or missing user");
        }
        String id = "ra-" + request.staffId() + "-" + request.role().toLowerCase() + "-" + request.scopeType() + "-" + request.scopeId().replace("*", "platform");
        PlatformUserDto user = users.get(request.staffId());
        RoleAssignmentDto dto = new RoleAssignmentDto(
                id,
                request.staffId(),
                user.displayName(),
                request.role(),
                request.scopeType(),
                request.scopeId(),
                grantedBy,
                Instant.now()
        );
        assignments.put(id, dto);
        return dto;
    }

    public void revoke(String assignmentId) {
        RoleAssignmentDto target = assignments.get(assignmentId);
        if (target == null) {
            return;
        }
        if ("PLATFORM_ADMIN".equals(target.role())) {
            long activeAdmins = assignments.values().stream()
                    .filter(item -> "PLATFORM_ADMIN".equals(item.role()))
                    .filter(item -> isActiveUser(item.staffId()))
                    .count();
            if (activeAdmins <= 1) {
                throw new IllegalStateException("LAST_PLATFORM_ADMIN");
            }
        }
        assignments.remove(assignmentId);
    }

    private void seed() {
        Instant now = Instant.parse("2026-05-02T00:00:00Z");
        users.put("43910516", new PlatformUserDto("43910516", "Platform Admin", null, null,
                "admin@sdlctower.local", "manual", null, "active", now, now));
        users.put("43910000", new PlatformUserDto("43910000", "Alice", "Alice Chen",
                "https://teambook.company.com/avatar/43910000", "alice@example.com", "teambook",
                now, "active", now, now));
        users.put("43910001", new PlatformUserDto("43910001", "Backup Admin", null, null,
                "backup-admin@sdlctower.local", "manual", null, "active", now, now));
        List<RoleAssignmentDto> seeded = new ArrayList<>();
        seeded.add(new RoleAssignmentDto("ra-admin-platform", "43910516", "Platform Admin", "PLATFORM_ADMIN", "platform", "*", "system", now));
        seeded.add(new RoleAssignmentDto("ra-backup-admin-platform", "43910001", "Backup Admin", "PLATFORM_ADMIN", "platform", "*", "system", now));
        seeded.add(new RoleAssignmentDto("ra-alice-app", "43910000", "Alice", "WORKSPACE_MEMBER", "application", "app-payment-gateway-pro", "43910516", now));
        seeded.add(new RoleAssignmentDto("ra-alice-snow", "43910000", "Alice", "WORKSPACE_VIEWER", "snow_group", "snow-fin-tech-ops", "43910516", now));
        seeded.forEach(item -> assignments.put(item.id(), item));
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
