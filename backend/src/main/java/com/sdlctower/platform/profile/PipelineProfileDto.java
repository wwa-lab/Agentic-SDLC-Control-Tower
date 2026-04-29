package com.sdlctower.platform.profile;

import java.util.List;

public record PipelineProfileDto(
        String id,
        String name,
        String description,
        List<PipelineChainNodeDto> chainNodes,
        List<PipelineSkillBindingDto> skills,
        List<PipelineEntryPathDto> entryPaths,
        List<PipelineDocumentStageDto> documentStages,
        PipelineSpecTieringDto specTiering,
        boolean usesOrchestrator,
        String traceabilityMode
) {}
