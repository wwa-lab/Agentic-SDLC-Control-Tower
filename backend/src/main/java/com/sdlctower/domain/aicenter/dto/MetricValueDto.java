package com.sdlctower.domain.aicenter.dto;

public record MetricValueDto(
        double value,
        String unit,
        double delta,
        String trend,
        boolean isPositive
) {}
