package com.sdlctower.domain.aicenter.dto;

import java.util.List;

public record SkillDetailDto(
        SkillDto skill,
        String inputContract,
        String outputContract,
        PolicyDto policy,
        List<RunDto> recentRuns,
        AggregateMetricsDto aggregateMetrics
) {}
