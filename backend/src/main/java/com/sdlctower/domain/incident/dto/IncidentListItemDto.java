package com.sdlctower.domain.incident.dto;

public record IncidentListItemDto(
        String id,
        String title,
        String priority,
        String status,
        String handlerType,
        String controlMode,
        String detectedAt,
        String duration
) {}
