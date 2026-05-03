package com.sdlctower.platform.template;

import java.time.Instant;

public record TemplateSummaryDto(
        String id,
        String key,
        String name,
        String kind,
        String status,
        Integer version,
        String ownerId,
        Instant lastModifiedAt
) {}
