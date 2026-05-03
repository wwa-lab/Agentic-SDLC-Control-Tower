package com.sdlctower.platform.support;

public record SupportRequestResultDto(
        String requestId,
        String status,
        String jiraKey,
        String jiraUrl
) {}
