package com.sdlctower.platform.policy;

import com.fasterxml.jackson.databind.JsonNode;

public record UpsertPolicyRequest(
        String key,
        String name,
        String category,
        String scopeType,
        String scopeId,
        String boundTo,
        String status,
        JsonNode body
) {}
