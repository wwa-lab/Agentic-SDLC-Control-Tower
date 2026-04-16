package com.sdlctower.domain.incident.dto;

public record IncidentHeaderDto(
        String id,
        String title,
        String priority,
        String status,
        String handlerType,
        String controlMode,
        String autonomyLevel,
        String detectedAt,
        String acknowledgedAt,
        String resolvedAt,
        String duration
) {}
