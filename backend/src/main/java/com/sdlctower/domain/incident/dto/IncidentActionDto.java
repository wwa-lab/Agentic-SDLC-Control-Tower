package com.sdlctower.domain.incident.dto;

public record IncidentActionDto(
        String id,
        String description,
        String actionType,
        String executionStatus,
        String timestamp,
        String impactAssessment,
        boolean isRollbackable,
        String policyRef
) {}
