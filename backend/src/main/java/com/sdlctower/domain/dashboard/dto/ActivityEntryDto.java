package com.sdlctower.domain.dashboard.dto;

public record ActivityEntryDto(
        String id,
        String actor,
        String actorType,
        String action,
        String stageKey,
        String timestamp
) {
}
