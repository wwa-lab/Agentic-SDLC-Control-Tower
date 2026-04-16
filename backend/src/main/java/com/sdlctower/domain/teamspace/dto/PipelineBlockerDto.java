package com.sdlctower.domain.teamspace.dto;

public record PipelineBlockerDto(
        String kind,
        String targetId,
        String targetTitle,
        int ageDays,
        String filterDeeplink
) {}
