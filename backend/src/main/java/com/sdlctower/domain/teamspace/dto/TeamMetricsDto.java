package com.sdlctower.domain.teamspace.dto;

import java.time.Instant;
import java.util.List;

public record TeamMetricsDto(
        List<TeamMetricItemDto> deliveryEfficiency,
        List<TeamMetricItemDto> quality,
        List<TeamMetricItemDto> stability,
        List<TeamMetricItemDto> governanceMaturity,
        List<TeamMetricItemDto> aiParticipation,
        Instant lastRefreshed
) {}
