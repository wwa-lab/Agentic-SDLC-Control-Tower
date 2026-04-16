package com.sdlctower.domain.requirement.dto;

public record SdlcChainLinkDto(
        String artifactType,
        String artifactId,
        String artifactTitle,
        String routePath
) {}
