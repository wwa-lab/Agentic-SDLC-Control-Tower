package com.sdlctower.domain.requirement.dto;

public record AcceptanceCriterionDto(
        String id,
        String text,
        boolean isMet
) {}
