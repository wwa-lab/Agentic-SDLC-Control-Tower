package com.sdlctower.domain.aicenter.dto;

public record StageCoverageEntryDto(
        String stageKey,
        String stageLabel,
        int activeSkillCount,
        boolean covered
) {}
