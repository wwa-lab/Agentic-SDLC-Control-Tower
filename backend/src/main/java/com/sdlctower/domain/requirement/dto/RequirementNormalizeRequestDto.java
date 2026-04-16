package com.sdlctower.domain.requirement.dto;

public record RequirementNormalizeRequestDto(
        RawRequirementInputDto rawInput,
        String profileId
) {}
