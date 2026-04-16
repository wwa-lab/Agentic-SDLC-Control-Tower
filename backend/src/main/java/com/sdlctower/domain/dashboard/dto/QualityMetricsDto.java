package com.sdlctower.domain.dashboard.dto;

public record QualityMetricsDto(
        MetricValueDto buildSuccessRate,
        MetricValueDto testPassRate,
        MetricValueDto defectDensity,
        MetricValueDto specCoverage
) {
}
