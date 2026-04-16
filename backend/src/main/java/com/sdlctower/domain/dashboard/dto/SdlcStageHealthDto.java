package com.sdlctower.domain.dashboard.dto;

public record SdlcStageHealthDto(
        String key,
        String label,
        String status,
        int itemCount,
        boolean isHub,
        String routePath
) {
}
