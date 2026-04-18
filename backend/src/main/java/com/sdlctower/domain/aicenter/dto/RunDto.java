package com.sdlctower.domain.aicenter.dto;

import java.time.Instant;

public record RunDto(
        String id,
        String skillKey,
        String skillName,
        String status,
        String triggerSourceType,
        String triggerSourcePage,
        String triggerSourceUrl,
        String triggeredBy,
        String triggeredByType,
        Instant startedAt,
        Instant endedAt,
        Long durationMs,
        String outcomeSummary,
        String auditRecordId
) {}
