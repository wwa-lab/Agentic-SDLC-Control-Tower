package com.sdlctower.platform.workspace;

/**
 * Immutable runtime workspace context carried by WorkspaceContextHolder per HTTP request.
 * This record is NOT a JPA entity — see WorkspaceContextEntity for the legacy single-row table.
 */
public record WorkspaceContext(
        String workspaceId,
        String workspaceKey,
        String workspaceName,
        String applicationId,
        String snowGroupId,
        String profileId,
        boolean demoMode
) {}
