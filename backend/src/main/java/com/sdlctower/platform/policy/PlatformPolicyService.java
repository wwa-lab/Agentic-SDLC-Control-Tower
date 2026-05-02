package com.sdlctower.platform.policy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.platform.audit.AuditEvent;
import com.sdlctower.platform.audit.AuditWriter;
import com.sdlctower.platform.shared.CursorPageDto;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformPolicyService {

    private static final Set<String> CATEGORIES = Set.of("action", "approval", "autonomy", "risk-threshold", "exception");
    private static final Set<String> SCOPE_TYPES = Set.of("platform", "application", "snow_group", "workspace", "project");
    private static final Set<String> STATUSES = Set.of("draft", "active", "inactive");

    private final PlatformPolicyRepository policies;
    private final PlatformPolicyExceptionRepository exceptions;
    private final AuditWriter auditWriter;
    private final ObjectMapper objectMapper;

    public PlatformPolicyService(PlatformPolicyRepository policies, PlatformPolicyExceptionRepository exceptions, AuditWriter auditWriter, ObjectMapper objectMapper) {
        this.policies = policies;
        this.exceptions = exceptions;
        this.auditWriter = auditWriter;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CursorPageDto<PolicyDto> list(String category, String status, String scopeType, String scopeId, String boundTo, String q) {
        return CursorPageDto.of(policies.search(
                        blankToNull(category),
                        blankToNull(status),
                        blankToNull(scopeType),
                        blankToNull(scopeId),
                        blankToNull(boundTo),
                        blankToNull(q)
                ).stream()
                .map(this::toDto)
                .toList());
    }

    @Transactional(readOnly = true)
    public PolicyDto detail(String id) {
        return policies.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new PlatformPolicyException("POLICY_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PolicyExceptionDto> exceptions(String policyId) {
        requirePolicy(policyId);
        return exceptions.findByPolicyIdOrderByCreatedAtDesc(policyId).stream()
                .map(this::toExceptionDto)
                .toList();
    }

    @Transactional
    public PolicyDto create(UpsertPolicyRequest request, String actorStaffId) {
        validatePolicy(request);
        String key = request.key().trim();
        String scopeType = request.scopeType().trim();
        String scopeId = normalizedScopeId(scopeType, request.scopeId());
        String status = normalizedStatus(request.status());
        if ("active".equals(status)) {
            policies.findByKeyAndScopeTypeAndScopeIdAndStatus(key, scopeType, scopeId, "active").ifPresent(existing -> {
                throw new PlatformPolicyException("POLICY_ALREADY_EXISTS", HttpStatus.CONFLICT);
            });
        }

        PlatformPolicyEntity entity = new PlatformPolicyEntity();
        entity.setId("pol-" + UUID.randomUUID());
        apply(entity, request, 1, status, actorStaffId, Instant.now());
        PolicyDto saved = toDto(policies.save(entity));
        writeAudit(actorStaffId, "policy.create", saved, null, saved, request);
        return saved;
    }

    @Transactional
    public PolicyDto update(String id, UpsertPolicyRequest request, String actorStaffId) {
        validatePolicy(request);
        PlatformPolicyEntity source = requirePolicy(id);
        PolicyDto before = toDto(source);
        String key = request.key().trim();
        String scopeType = request.scopeType().trim();
        String scopeId = normalizedScopeId(scopeType, request.scopeId());
        String status = "active";
        policies.findByKeyAndScopeTypeAndScopeIdAndStatus(key, scopeType, scopeId, "active")
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new PlatformPolicyException("POLICY_ALREADY_EXISTS", HttpStatus.CONFLICT);
                });

        source.setStatus("inactive");
        policies.save(source);

        PlatformPolicyEntity next = new PlatformPolicyEntity();
        next.setId("pol-" + UUID.randomUUID());
        apply(next, request, source.getVersionNumber() + 1, status, actorStaffId, Instant.now());
        PolicyDto saved = toDto(policies.save(next));
        writeAudit(actorStaffId, "policy.update", saved, before, saved, request);
        return saved;
    }

    @Transactional
    public PolicyDto activate(String id, String actorStaffId) {
        PlatformPolicyEntity entity = requirePolicy(id);
        if (!"active".equals(entity.getStatus())) {
            policies.findByKeyAndScopeTypeAndScopeIdAndStatus(entity.getKey(), entity.getScopeType(), entity.getScopeId(), "active")
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new PlatformPolicyException("POLICY_ALREADY_EXISTS", HttpStatus.CONFLICT);
                    });
        }
        PolicyDto before = toDto(entity);
        entity.setStatus("active");
        PolicyDto saved = toDto(policies.save(entity));
        writeAudit(actorStaffId, "policy.activate", saved, before, saved, Map.of("id", id));
        return saved;
    }

    @Transactional
    public PolicyDto deactivate(String id, String actorStaffId) {
        PlatformPolicyEntity entity = requirePolicy(id);
        PolicyDto before = toDto(entity);
        entity.setStatus("inactive");
        PolicyDto saved = toDto(policies.save(entity));
        writeAudit(actorStaffId, "policy.deactivate", saved, before, saved, Map.of("id", id));
        return saved;
    }

    @Transactional
    public PolicyExceptionDto addException(String policyId, CreatePolicyExceptionRequest request, String actorStaffId) {
        PlatformPolicyEntity policy = requirePolicy(policyId);
        if (!"active".equals(policy.getStatus())) {
            throw new PlatformPolicyException("POLICY_NOT_ACTIVE", HttpStatus.CONFLICT);
        }
        validateException(request);

        PlatformPolicyExceptionEntity entity = new PlatformPolicyExceptionEntity();
        entity.setId("pex-" + UUID.randomUUID());
        entity.setPolicyId(policyId);
        entity.setReason(request.reason().trim());
        entity.setRequesterId(request.requesterId().trim());
        entity.setApproverId(request.approverId().trim());
        entity.setCreatedAt(Instant.now());
        entity.setExpiresAt(request.expiresAt());
        PolicyExceptionDto saved = toExceptionDto(exceptions.save(entity));
        writeAudit(actorStaffId, "policy.exception.add", toDto(policy), null, saved, request);
        return saved;
    }

    @Transactional
    public void revokeException(String policyId, String exceptionId, String actorStaffId) {
        PlatformPolicyEntity policy = requirePolicy(policyId);
        PlatformPolicyExceptionEntity entity = exceptions.findByIdAndPolicyId(exceptionId, policyId)
                .orElseThrow(() -> new PlatformPolicyException("POLICY_EXCEPTION_NOT_FOUND", HttpStatus.NOT_FOUND));
        PolicyExceptionDto before = toExceptionDto(entity);
        if (entity.getRevokedAt() == null) {
            entity.setRevokedAt(Instant.now());
        }
        PolicyExceptionDto saved = toExceptionDto(exceptions.save(entity));
        writeAudit(actorStaffId, "policy.exception.revoke", toDto(policy), before, saved, Map.of("id", exceptionId));
    }

    private PlatformPolicyEntity requirePolicy(String id) {
        return policies.findById(id)
                .orElseThrow(() -> new PlatformPolicyException("POLICY_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private void apply(PlatformPolicyEntity entity, UpsertPolicyRequest request, int version, String status, String actorStaffId, Instant now) {
        String scopeType = request.scopeType().trim();
        entity.setKey(request.key().trim());
        entity.setName(request.name().trim());
        entity.setCategory(request.category().trim());
        entity.setScopeType(scopeType);
        entity.setScopeId(normalizedScopeId(scopeType, request.scopeId()));
        entity.setBoundTo(blankToNull(request.boundTo()));
        entity.setVersionNumber(version);
        entity.setStatus(status);
        entity.setBody(writeJson(request.body()));
        entity.setCreatedAt(now);
        entity.setCreatedBy(defaultString(actorStaffId, "system"));
    }

    private void validatePolicy(UpsertPolicyRequest request) {
        if (request == null || blankToNull(request.key()) == null || blankToNull(request.name()) == null) {
            throw new PlatformPolicyException("INVALID_POLICY_BODY", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!CATEGORIES.contains(defaultString(request.category(), ""))) {
            throw new PlatformPolicyException("INVALID_POLICY_CATEGORY", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!SCOPE_TYPES.contains(defaultString(request.scopeType(), ""))) {
            throw new PlatformPolicyException("INVALID_POLICY_SCOPE", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        normalizedScopeId(request.scopeType(), request.scopeId());
        if (!STATUSES.contains(normalizedStatus(request.status()))) {
            throw new PlatformPolicyException("INVALID_POLICY_STATUS", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (request.body() == null || !request.body().isObject()) {
            throw new PlatformPolicyException("INVALID_POLICY_BODY", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void validateException(CreatePolicyExceptionRequest request) {
        if (request == null || blankToNull(request.reason()) == null || blankToNull(request.requesterId()) == null || blankToNull(request.approverId()) == null) {
            throw new PlatformPolicyException("INVALID_POLICY_EXCEPTION", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (request.expiresAt() == null || !request.expiresAt().isAfter(Instant.now())) {
            throw new PlatformPolicyException("INVALID_POLICY_EXCEPTION", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private PolicyDto toDto(PlatformPolicyEntity entity) {
        return new PolicyDto(
                entity.getId(),
                entity.getKey(),
                entity.getName(),
                entity.getCategory(),
                entity.getScopeType(),
                entity.getScopeId(),
                entity.getBoundTo(),
                entity.getVersionNumber(),
                entity.getStatus(),
                readJson(entity.getBody()),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    private PolicyExceptionDto toExceptionDto(PlatformPolicyExceptionEntity entity) {
        return new PolicyExceptionDto(
                entity.getId(),
                entity.getPolicyId(),
                entity.getReason(),
                entity.getRequesterId(),
                entity.getApproverId(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt()
        );
    }

    private void writeAudit(String actorStaffId, String action, PolicyDto policy, Object before, Object after, Object request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", before);
        payload.put("after", after);
        payload.put("request", request);
        payload.put("source", "platform-policy");
        String actor = defaultString(actorStaffId, "system");
        auditWriter.write(new AuditEvent(
                actor,
                "system".equals(actor) ? "system" : "user",
                "config_change",
                action,
                "policy",
                policy.id(),
                policy.scopeType(),
                policy.scopeId(),
                "success",
                null,
                payload
        ));
    }

    private JsonNode readJson(String value) {
        if (value == null || value.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(value);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (JsonProcessingException ex) {
            return objectMapper.createObjectNode().put("raw", value);
        }
    }

    private String writeJson(JsonNode body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            throw new PlatformPolicyException("INVALID_POLICY_BODY", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private String normalizedScopeId(String scopeType, String scopeId) {
        if ("platform".equals(scopeType)) {
            if (!"*".equals(scopeId)) {
                throw new PlatformPolicyException("INVALID_POLICY_SCOPE", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            return "*";
        }
        if (blankToNull(scopeId) == null) {
            throw new PlatformPolicyException("INVALID_POLICY_SCOPE", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return scopeId.trim();
    }

    private String normalizedStatus(String status) {
        return defaultString(status, "active").trim().toLowerCase();
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
