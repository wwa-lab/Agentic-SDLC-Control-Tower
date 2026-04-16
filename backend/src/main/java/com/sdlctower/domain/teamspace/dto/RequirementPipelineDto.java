package com.sdlctower.domain.teamspace.dto;

import java.util.List;

public record RequirementPipelineDto(
        PipelineCountersDto counters,
        List<PipelineBlockerDto> blockers,
        List<ChainNodeHealthDto> chain,
        int blockerThresholdDays
) {}
