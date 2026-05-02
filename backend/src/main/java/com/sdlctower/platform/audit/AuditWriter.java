package com.sdlctower.platform.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditWriter {

    private final AuditRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock = Clock.systemUTC();

    public AuditWriter(AuditRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public AuditRecordDto write(AuditEvent event) {
        AuditRecordEntity entity = new AuditRecordEntity();
        entity.setId("aud-" + UUID.randomUUID());
        entity.setEventTime(clock.instant());
        entity.setActor(defaultString(event.actor(), "system"));
        entity.setActorType(defaultString(event.actorType(), "system"));
        entity.setCategory(event.category());
        entity.setAction(event.action());
        entity.setObjectType(event.objectType());
        entity.setObjectId(event.objectId());
        entity.setScopeType(event.scopeType());
        entity.setScopeId(event.scopeId());
        entity.setOutcome(defaultString(event.outcome(), "success"));
        entity.setEvidenceRef(event.evidenceRef());
        entity.setPayload(writePayload(event.payload()));
        return AuditQueryService.toDto(repository.save(entity), objectMapper);
    }

    private String writePayload(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("AUDIT_PAYLOAD_INVALID", ex);
        }
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
