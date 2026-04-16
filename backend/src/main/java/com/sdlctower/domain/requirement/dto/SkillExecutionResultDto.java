package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SkillExecutionResultDto(
        String executionId,
        String skillName,
        String status,
        String requirementId,
        String startedAt,
        Integer estimatedCompletionSeconds,
        String message,
        OrchestratorResultDto orchestratorResult
) {}
