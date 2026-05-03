package com.sdlctower.platform.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.platform.audit.AuditEvent;
import com.sdlctower.platform.audit.AuditWriter;
import com.sdlctower.platform.shared.CursorPageDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformConfigurationService {

    private static final Set<String> KINDS = Set.of("page", "field", "component", "flow-rule", "view-rule", "notification", "ai-config");
    private static final Set<String> SCOPE_TYPES = Set.of("platform", "application", "snow_group", "workspace", "project");

    private final PlatformConfigurationRepository configurations;
    private final AuditWriter auditWriter;
    private final ObjectMapper objectMapper;

    public PlatformConfigurationService(PlatformConfigurationRepository configurations, AuditWriter auditWriter, ObjectMapper objectMapper) {
        this.configurations = configurations;
        this.auditWriter = auditWriter;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CursorPageDto<ConfigurationSummaryDto> list(String kind, String scopeType, String scopeId, String status, String q) {
        return CursorPageDto.of(configurations.search(
                        blankToNull(kind),
                        blankToNull(scopeType),
                        blankToNull(scopeId),
                        blankToNull(status),
                        blankToNull(q)
                )
                .stream()
                .map(entity -> toSummary(entity, driftFields(entity)))
                .toList());
    }

    @Transactional(readOnly = true)
    public ConfigurationDetailDto detail(String id) {
        PlatformConfigurationEntity entity = configurations.findById(id)
                .orElseThrow(() -> new PlatformConfigurationException("CONFIGURATION_NOT_FOUND", HttpStatus.NOT_FOUND));
        return toDetail(entity);
    }

    @Transactional
    public ConfigurationDetailDto create(UpsertConfigurationRequest request, String actorStaffId) {
        validate(request);
        String key = request.key().trim();
        String scopeType = request.scopeType().trim();
        String scopeId = normalizedScopeId(scopeType, request.scopeId());
        configurations.findByKeyAndScopeTypeAndScopeId(key, scopeType, scopeId).ifPresent(existing -> {
            throw new PlatformConfigurationException("CONFIGURATION_ALREADY_EXISTS", HttpStatus.CONFLICT);
        });

        PlatformConfigurationEntity entity = new PlatformConfigurationEntity();
        entity.setId("cfg-" + UUID.randomUUID());
        apply(entity, request, Instant.now());
        ConfigurationDetailDto saved = toDetail(configurations.save(entity));
        writeConfigAudit(actorStaffId, "configuration.create", null, saved, request);
        return saved;
    }

    @Transactional
    public ConfigurationDetailDto update(String id, UpsertConfigurationRequest request, String actorStaffId) {
        PlatformConfigurationEntity entity = configurations.findById(id)
                .orElseThrow(() -> new PlatformConfigurationException("CONFIGURATION_NOT_FOUND", HttpStatus.NOT_FOUND));
        validate(request);
        ConfigurationDetailDto before = toDetail(entity);
        String nextKey = request.key().trim();
        String nextScopeType = request.scopeType().trim();
        String nextScopeId = normalizedScopeId(nextScopeType, request.scopeId());
        configurations.findByKeyAndScopeTypeAndScopeId(nextKey, nextScopeType, nextScopeId)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new PlatformConfigurationException("CONFIGURATION_ALREADY_EXISTS", HttpStatus.CONFLICT);
                });

        boolean deactivated = "active".equalsIgnoreCase(entity.getStatus()) && "inactive".equalsIgnoreCase(request.status());
        apply(entity, request, Instant.now());
        ConfigurationDetailDto saved = toDetail(configurations.save(entity));
        writeConfigAudit(actorStaffId, deactivated ? "configuration.deactivate" : "configuration.update", before, saved, request);
        return saved;
    }

    private void apply(PlatformConfigurationEntity entity, UpsertConfigurationRequest request, Instant now) {
        String scopeType = request.scopeType().trim();
        String scopeId = normalizedScopeId(scopeType, request.scopeId());
        entity.setKey(request.key().trim());
        entity.setKind(request.kind().trim());
        entity.setScopeType(scopeType);
        entity.setScopeId(scopeId);
        entity.setParentId(blankToNull(request.parentId()));
        entity.setStatus(request.status().trim().toLowerCase());
        entity.setBody(writeJson(request.body()));
        entity.setLastModifiedAt(now);
        entity.setHasDrift(!driftFields(entity).isEmpty());
    }

    private void validate(UpsertConfigurationRequest request) {
        if (request == null || blankToNull(request.key()) == null) {
            throw new PlatformConfigurationException("CONFIGURATION_NOT_FOUND", HttpStatus.NOT_FOUND);
        }
        if (!KINDS.contains(request.kind())) {
            throw new PlatformConfigurationException("INVALID_CONFIG_KIND", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!SCOPE_TYPES.contains(request.scopeType())) {
            throw new PlatformConfigurationException("INVALID_CONFIG_SCOPE", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        normalizedScopeId(request.scopeType(), request.scopeId());
        if (!Set.of("active", "inactive").contains(defaultString(request.status(), "").toLowerCase())) {
            throw new PlatformConfigurationException("INVALID_CONFIG_STATUS", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (request.body() == null || !request.body().isObject()) {
            throw new PlatformConfigurationException("INVALID_CONFIG_BODY", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private ConfigurationSummaryDto toSummary(PlatformConfigurationEntity entity, List<String> driftFields) {
        return new ConfigurationSummaryDto(
                entity.getId(),
                entity.getKey(),
                entity.getKind(),
                entity.getScopeType(),
                entity.getScopeId(),
                entity.getParentId(),
                entity.getStatus(),
                !driftFields.isEmpty(),
                entity.getLastModifiedAt()
        );
    }

    private ConfigurationDetailDto toDetail(PlatformConfigurationEntity entity) {
        JsonNode body = readJson(entity.getBody());
        JsonNode platformDefault = platformDefaultBody(entity);
        List<String> driftFields = driftFields(body, platformDefault);
        return new ConfigurationDetailDto(
                entity.getId(),
                entity.getKey(),
                entity.getKind(),
                entity.getScopeType(),
                entity.getScopeId(),
                entity.getParentId(),
                entity.getStatus(),
                !driftFields.isEmpty(),
                entity.getLastModifiedAt(),
                body,
                platformDefault,
                driftFields
        );
    }

    private List<String> driftFields(PlatformConfigurationEntity entity) {
        return driftFields(readJson(entity.getBody()), platformDefaultBody(entity));
    }

    private JsonNode platformDefaultBody(PlatformConfigurationEntity entity) {
        if ("platform".equals(entity.getScopeType()) && "*".equals(entity.getScopeId())) {
            return readJson(entity.getBody());
        }
        return configurations.findByKeyAndScopeTypeAndScopeIdAndStatus(entity.getKey(), "platform", "*", "active")
                .map(defaultEntity -> readJson(defaultEntity.getBody()))
                .orElse(objectMapper.nullNode());
    }

    private List<String> driftFields(JsonNode body, JsonNode platformDefault) {
        if (platformDefault == null || platformDefault.isNull() || !body.isObject() || !platformDefault.isObject()) {
            return List.of();
        }
        Set<String> keys = new LinkedHashSet<>();
        body.fieldNames().forEachRemaining(keys::add);
        platformDefault.fieldNames().forEachRemaining(keys::add);
        List<String> drift = new ArrayList<>();
        for (String key : keys) {
            JsonNode selected = body.get(key);
            JsonNode platform = platformDefault.get(key);
            if (selected == null || platform == null || !selected.equals(platform)) {
                drift.add(key);
            }
        }
        return drift;
    }

    private void writeConfigAudit(String actorStaffId, String action, Object before, Object after, Object request) {
        ConfigurationDetailDto target = (ConfigurationDetailDto) after;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", before);
        payload.put("after", after);
        payload.put("request", request);
        payload.put("source", "platform-configuration");
        String actor = defaultString(actorStaffId, "system");
        auditWriter.write(new AuditEvent(
                actor,
                "system".equals(actor) ? "system" : "user",
                "config_change",
                action,
                "configuration",
                target.id(),
                target.scopeType(),
                target.scopeId(),
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
            throw new PlatformConfigurationException("INVALID_CONFIG_BODY", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private String normalizedScopeId(String scopeType, String scopeId) {
        if ("platform".equals(scopeType)) {
            if (!"*".equals(scopeId)) {
                throw new PlatformConfigurationException("INVALID_CONFIG_SCOPE", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            return "*";
        }
        if (blankToNull(scopeId) == null) {
            throw new PlatformConfigurationException("INVALID_CONFIG_SCOPE", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return scopeId.trim();
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
