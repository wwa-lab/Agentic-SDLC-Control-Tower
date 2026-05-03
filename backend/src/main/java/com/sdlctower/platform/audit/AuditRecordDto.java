package com.sdlctower.platform.audit;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record AuditRecordDto(
        String id,
        Instant timestamp,
        String actor,
        String actorType,
        String category,
        String action,
        String objectType,
        String objectId,
        String scopeType,
        String scopeId,
        String outcome,
        String evidenceRef,
        JsonNode payload
) {}
