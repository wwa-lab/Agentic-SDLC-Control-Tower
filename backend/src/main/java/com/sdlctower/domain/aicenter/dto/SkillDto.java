package com.sdlctower.domain.aicenter.dto;

import java.time.Instant;
import java.util.List;

public record SkillDto(
        String id,
        String key,
        String name,
        String category,
        String subCategory,
        String status,
        String defaultAutonomy,
        String owner,
        String description,
        List<String> stages,
        Instant lastExecutedAt,
        Double successRate30d,
        int version
) {}
