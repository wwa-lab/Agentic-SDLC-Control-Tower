package com.sdlctower.domain.dashboard.dto;

import java.util.List;

public record ValueStoryDto(
        String headline,
        List<ValueStoryMetricDto> metrics
) {
}
