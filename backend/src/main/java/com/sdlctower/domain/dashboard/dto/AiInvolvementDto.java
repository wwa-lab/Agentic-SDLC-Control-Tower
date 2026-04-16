package com.sdlctower.domain.dashboard.dto;

public record AiInvolvementDto(
        String stageKey,
        boolean involved,
        int actionsCount
) {
}
