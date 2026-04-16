package com.sdlctower.domain.dashboard.dto;

public record StabilityMetricsDto(
        int activeIncidents,
        int criticalIncidents,
        MetricValueDto changeFailureRate,
        MetricValueDto mttr,
        MetricValueDto rollbackRate
) {
}
