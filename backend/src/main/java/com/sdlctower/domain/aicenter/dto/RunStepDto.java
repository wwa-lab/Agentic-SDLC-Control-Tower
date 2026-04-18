package com.sdlctower.domain.aicenter.dto;

import java.time.Instant;

public record RunStepDto(
        int ordinal,
        String name,
        String status,
        Instant startedAt,
        Instant endedAt,
        Long durationMs,
        String note
) {}
