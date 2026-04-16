package com.sdlctower.domain.incident.dto;

public record SdlcChainLinkDto(
        String artifactType,
        String artifactId,
        String artifactTitle,
        String routePath
) {}
