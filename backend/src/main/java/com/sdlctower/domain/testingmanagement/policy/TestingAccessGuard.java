package com.sdlctower.domain.testingmanagement.policy;

import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.testingmanagement.TestingManagementConstants;
import org.springframework.stereotype.Component;

@Component("testingManagementAccessGuard")
public class TestingAccessGuard {

    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;
    private final TeamSpaceSeedCatalog teamSpaceSeedCatalog;

    public TestingAccessGuard(
            ProjectSpaceSeedCatalog projectSpaceSeedCatalog,
            TeamSpaceSeedCatalog teamSpaceSeedCatalog
    ) {
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
        this.teamSpaceSeedCatalog = teamSpaceSeedCatalog;
    }

    public void requireWorkspaceRead(String workspaceId) {
        if (!TestingManagementConstants.WORKSPACE_ID_PATTERN.matcher(workspaceId).matches()) {
            throw TestingManagementException.badRequest("TM_VALIDATION_ERROR", "Invalid workspaceId: " + workspaceId);
        }
        teamSpaceSeedCatalog.workspace(workspaceId);
        if ("ws-private-001".equals(workspaceId)) {
            throw TestingManagementException.forbidden("TM_WORKSPACE_FORBIDDEN", "Workspace access denied: " + workspaceId);
        }
    }

    public void requireProjectRead(String projectId) {
        if (!TestingManagementConstants.PROJECT_ID_PATTERN.matcher(projectId).matches()) {
            throw TestingManagementException.badRequest("TM_VALIDATION_ERROR", "Invalid projectId: " + projectId);
        }
        String workspaceId = projectSpaceSeedCatalog.project(projectId).workspaceId();
        requireWorkspaceRead(workspaceId);
    }
}
