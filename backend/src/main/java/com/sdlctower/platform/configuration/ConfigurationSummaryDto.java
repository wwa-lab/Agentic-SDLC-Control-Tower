package com.sdlctower.platform.configuration;

import java.time.Instant;

public record ConfigurationSummaryDto(
        String id,
        String key,
        String kind,
        String scopeType,
        String scopeId,
        String parentId,
        String status,
        Boolean hasDrift,
        Instant lastModifiedAt
) {}
