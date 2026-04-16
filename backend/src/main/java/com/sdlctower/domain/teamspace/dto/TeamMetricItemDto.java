package com.sdlctower.domain.teamspace.dto;

public record TeamMetricItemDto(
        String key,
        String label,
        double currentValue,
        double previousValue,
        String unit,
        String trend,
        String historyLink,
        String tooltip
) {}
