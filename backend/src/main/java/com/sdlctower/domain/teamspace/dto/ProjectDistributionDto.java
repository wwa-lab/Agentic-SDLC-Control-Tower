package com.sdlctower.domain.teamspace.dto;

import java.util.List;
import java.util.Map;

public record ProjectDistributionDto(
        Map<String, List<ProjectCardDto>> groups,
        Map<String, Integer> totals
) {}
