package com.sdlctower.platform.workspace;

public record WorkspaceListItemDto(
        String workspaceId,
        String workspaceKey,
        String name,
        String applicationId,
        String snowGroupId,
        String profileId
) {}
