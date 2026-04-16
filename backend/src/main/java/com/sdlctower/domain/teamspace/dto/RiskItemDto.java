package com.sdlctower.domain.teamspace.dto;

public record RiskItemDto(
        String id,
        String category,
        String severity,
        String title,
        String detail,
        int ageDays,
        RiskActionDto primaryAction,
        SkillAttributionDto skillAttribution
) {}
