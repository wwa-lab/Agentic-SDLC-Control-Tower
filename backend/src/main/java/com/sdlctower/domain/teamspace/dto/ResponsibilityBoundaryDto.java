package com.sdlctower.domain.teamspace.dto;

import java.util.List;

public record ResponsibilityBoundaryDto(
        List<String> applications,
        List<String> snowGroups,
        int projectCount
) {}
