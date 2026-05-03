package com.sdlctower.platform.template;

import java.util.Map;

public record TemplateDetailDto(
        TemplateDto template,
        TemplateVersionDto version,
        Map<String, InheritanceFieldDto> inheritance
) {
    public record TemplateDto(
            String id,
            String key,
            String name,
            String kind,
            String status,
            Integer version,
            String ownerId,
            java.time.Instant lastModifiedAt,
            String description
    ) {}
}
