package com.sdlctower.platform.configuration;

import com.fasterxml.jackson.databind.JsonNode;

public record UpsertConfigurationRequest(
        String key,
        String kind,
        String scopeType,
        String scopeId,
        String parentId,
        String status,
        JsonNode body
) {}
