package com.sdlctower.platform.profile;

public record PipelineChainNodeDto(
        String id,
        String label,
        String artifactType,
        boolean isExecutionHub
) {}
