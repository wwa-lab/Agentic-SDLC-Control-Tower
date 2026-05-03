package com.sdlctower.platform.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;

public record ConfigurationDetailDto(
        String id,
        String key,
        String kind,
        String scopeType,
        String scopeId,
        String parentId,
        String status,
        Boolean hasDrift,
        Instant lastModifiedAt,
        JsonNode body,
        JsonNode platformDefaultBody,
        List<String> driftFields
) {}
