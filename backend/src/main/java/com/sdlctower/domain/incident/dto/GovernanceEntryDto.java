package com.sdlctower.domain.incident.dto;

public record GovernanceEntryDto(
        String actor,
        String timestamp,
        String actionTaken,
        String reason,
        String policyRef
) {}
