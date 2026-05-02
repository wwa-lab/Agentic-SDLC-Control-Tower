package com.sdlctower.platform.policy;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record PolicyDto(
        String id,
        String key,
        String name,
        String category,
        String scopeType,
        String scopeId,
        String boundTo,
        Integer version,
        String status,
        JsonNode body,
        Instant createdAt,
        String createdBy
) {}
