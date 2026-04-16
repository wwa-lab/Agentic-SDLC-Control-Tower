package com.sdlctower.domain.teamspace.dto;

import java.time.Instant;

public record ExceptionOverrideDto(
        String templateId,
        String templateName,
        String overrideScope,
        String overrideScopeId,
        String overrideScopeName,
        Instant overriddenAt,
        String overriddenBy
) {}
