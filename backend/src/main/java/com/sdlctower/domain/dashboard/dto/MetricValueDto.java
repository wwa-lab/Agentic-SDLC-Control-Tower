package com.sdlctower.domain.dashboard.dto;

public record MetricValueDto(
        String label,
        String value,
        String trend,
        boolean trendIsPositive
) {
}
