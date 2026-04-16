package com.sdlctower.domain.teamspace.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record TeamRiskRadarDto(
        Map<String, List<RiskItemDto>> groups,
        Instant lastRefreshed,
        int total
) {}
