package com.sdlctower.domain.dashboard.dto;

public record DeliveryMetricsDto(
        MetricValueDto leadTime,
        MetricValueDto deployFrequency,
        MetricValueDto iterationCompletion,
        String bottleneckStage
) {
}
