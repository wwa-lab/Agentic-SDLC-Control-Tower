package com.sdlctower.domain.aicenter.dto;

public record AggregateMetricsDto(
        double successRate,
        long avgDurationMs,
        String adoptionTrend,
        long totalRuns30d
) {}
