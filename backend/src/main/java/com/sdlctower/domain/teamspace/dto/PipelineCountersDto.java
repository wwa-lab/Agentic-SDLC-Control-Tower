package com.sdlctower.domain.teamspace.dto;

public record PipelineCountersDto(
        int requirementsInflow7d,
        int storiesDecomposing,
        int specsGenerating,
        int specsInReview,
        int specsBlocked,
        int specsApprovedAwaitingDownstream
) {}
