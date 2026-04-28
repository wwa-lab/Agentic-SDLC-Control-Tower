package com.sdlctower.platform.profile;

public record PipelineDocumentStageDto(
        String sddType,
        String label,
        String pathPattern,
        String artifactType,
        String expectedTier,
        String traceabilityKey
) {}
