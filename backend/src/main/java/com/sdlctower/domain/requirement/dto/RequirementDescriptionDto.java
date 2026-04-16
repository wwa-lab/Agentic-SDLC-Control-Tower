package com.sdlctower.domain.requirement.dto;

import java.util.List;

public record RequirementDescriptionDto(
        String summary,
        String businessJustification,
        List<AcceptanceCriterionDto> acceptanceCriteria,
        List<String> assumptions,
        List<String> constraints
) {}
