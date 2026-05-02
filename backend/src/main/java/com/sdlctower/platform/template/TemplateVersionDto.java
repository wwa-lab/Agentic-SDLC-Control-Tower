package com.sdlctower.platform.template;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record TemplateVersionDto(
        String id,
        String templateId,
        Integer version,
        JsonNode body,
        Instant createdAt,
        String createdBy
) {}
