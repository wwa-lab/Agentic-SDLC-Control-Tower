package com.sdlctower.domain.incident.dto;

public record ActionApprovalResultDto(
        String actionId,
        String newStatus,
        GovernanceEntryDto governanceEntry
) {}
