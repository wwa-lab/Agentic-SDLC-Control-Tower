package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenerationResultDto(
        String executionId,
        String skillName,
        String status,
        String requirementId,
        List<String> inputStoryIds,
        String startedAt,
        Integer estimatedCompletionSeconds,
        String message
) {}
