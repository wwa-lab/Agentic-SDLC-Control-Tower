package com.sdlctower.domain.incident.dto;

import java.util.List;

public record IncidentListDto(SeverityDistributionDto severityDistribution, List<IncidentListItemDto> incidents) {}
