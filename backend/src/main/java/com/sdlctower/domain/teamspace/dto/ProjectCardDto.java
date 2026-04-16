package com.sdlctower.domain.teamspace.dto;

public record ProjectCardDto(
        String id,
        String name,
        String lifecycleStage,
        String healthStratum,
        String primaryRisk,
        int activeSpecCount,
        int openIncidentCount,
        String projectSpaceUrl
) {}
