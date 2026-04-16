package com.sdlctower.domain.requirement.dto;

import java.util.List;

public record LinkedStoriesSectionDto(
        List<LinkedStoryDto> stories,
        int totalCount
) {}
