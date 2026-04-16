package com.sdlctower.domain.requirement.dto;

import java.util.List;

public record LinkedSpecsSectionDto(
        List<LinkedSpecDto> specs,
        int totalCount
) {}
