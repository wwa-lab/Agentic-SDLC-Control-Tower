package com.sdlctower.platform.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.platform.shared.CursorPageDto;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditQueryService {

    private final AuditRepository repository;
    private final ObjectMapper objectMapper;

    public AuditQueryService(AuditRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CursorPageDto<AuditRecordDto> list(
            String category,
            String actor,
            String objectType,
            String objectId,
            String outcome,
            String scopeType,
            String scopeId,
            String timeRange
    ) {
        List<AuditRecordDto> rows = repository.search(
                        blankToNull(category),
                        blankToNull(actor),
                        blankToNull(objectType),
                        blankToNull(objectId),
                        blankToNull(outcome),
                        blankToNull(scopeType),
                        blankToNull(scopeId),
                        fromTime(timeRange)
                )
                .stream()
                .map(entity -> toDto(entity, objectMapper))
                .toList();
        return CursorPageDto.of(rows);
    }

    @Transactional(readOnly = true)
    public AuditRecordDto detail(String id) {
        return repository.findById(id)
                .map(entity -> toDto(entity, objectMapper))
                .orElseThrow(() -> new ResourceNotFoundException("AUDIT_NOT_FOUND"));
    }

    static AuditRecordDto toDto(AuditRecordEntity entity, ObjectMapper objectMapper) {
        return new AuditRecordDto(
                entity.getId(),
                entity.getEventTime(),
                entity.getActor(),
                entity.getActorType(),
                entity.getCategory(),
                entity.getAction(),
                entity.getObjectType(),
                entity.getObjectId(),
                entity.getScopeType(),
                entity.getScopeId(),
                entity.getOutcome(),
                entity.getEvidenceRef(),
                readPayload(entity.getPayload(), objectMapper)
        );
    }

    private static JsonNode readPayload(String payload, ObjectMapper objectMapper) {
        if (payload == null || payload.isBlank()) {
            return objectMapper.nullNode();
        }
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            return objectMapper.createObjectNode().put("raw", payload);
        }
    }

    private Instant fromTime(String timeRange) {
        if (timeRange == null || timeRange.isBlank()) {
            return null;
        }
        Instant now = Instant.now();
        return switch (timeRange) {
            case "24h" -> now.minus(24, ChronoUnit.HOURS);
            case "7d" -> now.minus(7, ChronoUnit.DAYS);
            case "30d" -> now.minus(30, ChronoUnit.DAYS);
            default -> null;
        };
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
