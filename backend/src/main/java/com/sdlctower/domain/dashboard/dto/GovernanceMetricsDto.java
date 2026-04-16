package com.sdlctower.domain.dashboard.dto;

public record GovernanceMetricsDto(
        MetricValueDto templateReuse,
        MetricValueDto configDrift,
        MetricValueDto auditCoverage,
        MetricValueDto policyHitRate
) {
}
