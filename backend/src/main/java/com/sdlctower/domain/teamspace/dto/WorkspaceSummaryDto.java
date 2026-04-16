package com.sdlctower.domain.teamspace.dto;

public record WorkspaceSummaryDto(
        String id,
        String name,
        String applicationId,
        String applicationName,
        String snowGroupId,
        String snowGroupName,
        int activeProjectCount,
        int activeEnvironmentCount,
        String healthAggregate,
        String ownerId,
        String ownerDisplayName,
        boolean compatibilityMode,
        ResponsibilityBoundaryDto responsibilityBoundary
) {}
