package com.sdlctower.domain.teamspace;

import org.springframework.stereotype.Component;

@Component
public class WorkspaceAccessGuard {

    private final TeamSpaceSeedCatalog seedCatalog;

    public WorkspaceAccessGuard(TeamSpaceSeedCatalog seedCatalog) {
        this.seedCatalog = seedCatalog;
    }

    public void check(String workspaceId) {
        if (!TeamSpaceConstants.WORKSPACE_ID_PATTERN.matcher(workspaceId).matches()) {
            throw new IllegalArgumentException("Invalid workspaceId: " + workspaceId);
        }
        if ("ws-private-001".equals(workspaceId) && seedCatalog.exists(workspaceId)) {
            throw new WorkspaceAccessDeniedException("Workspace access denied: " + workspaceId);
        }
    }
}
