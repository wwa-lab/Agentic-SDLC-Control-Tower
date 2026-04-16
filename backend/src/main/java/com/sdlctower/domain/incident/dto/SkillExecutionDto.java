package com.sdlctower.domain.incident.dto;

public record SkillExecutionDto(
        String skillName,
        String startTime,
        String endTime,
        String status,
        String inputSummary,
        String outputSummary
) {}
