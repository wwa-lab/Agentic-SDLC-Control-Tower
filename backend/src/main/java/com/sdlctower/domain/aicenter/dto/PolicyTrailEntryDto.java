package com.sdlctower.domain.aicenter.dto;

import java.time.Instant;

public record PolicyTrailEntryDto(
        String rule,
        String decision,
        Instant at,
        String note
) {}
