package com.sdlctower.domain.codebuildmanagement.policy;

import com.sdlctower.domain.codebuildmanagement.CodeBuildManagementConstants;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementAccessGuard")
public class CodeBuildAccessGuard {

    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;
    private final TeamSpaceSeedCatalog teamSpaceSeedCatalog;

    public CodeBuildAccessGuard(
            ProjectSpaceSeedCatalog projectSpaceSeedCatalog,
            TeamSpaceSeedCatalog teamSpaceSeedCatalog
    ) {
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
        this.teamSpaceSeedCatalog = teamSpaceSeedCatalog;
    }

    public void requireRead(String projectId) {
        if (!CodeBuildManagementConstants.PROJECT_ID_PATTERN.matcher(projectId).matches()) {
            throw CodeBuildManagementException.badRequest(
                    "CB_VALIDATION_ERROR", "Invalid projectId: " + projectId);
        }
        String workspaceId = projectSpaceSeedCatalog.project(projectId).workspaceId();
        requireWorkspaceRead(workspaceId);
    }

    public void requireWorkspaceRead(String workspaceId) {
        if (!CodeBuildManagementConstants.WORKSPACE_ID_PATTERN.matcher(workspaceId).matches()) {
            throw CodeBuildManagementException.badRequest(
                    "CB_VALIDATION_ERROR", "Invalid workspaceId: " + workspaceId);
        }
        teamSpaceSeedCatalog.workspace(workspaceId);
        if ("ws-private-001".equals(workspaceId)) {
            throw CodeBuildManagementException.forbidden(
                    "CB_WORKSPACE_FORBIDDEN", "Workspace access denied: " + workspaceId);
        }
    }

    public void requireAdmin(String projectId, String memberId) {
        if (!CodeBuildManagementConstants.MEMBER_ID_PATTERN.matcher(memberId).matches()) {
            throw CodeBuildManagementException.badRequest(
                    "CB_VALIDATION_ERROR", "Invalid memberId: " + memberId);
        }
        requireRead(projectId);
    }

    /**
     * Returns true if the member holds PM or Tech Lead role.
     * V1 placeholder: treats mem-pm-001 and mem-lead-001 as admins.
     */
    public boolean isAdmin(String memberId) {
        return "mem-pm-001".equals(memberId) || "mem-lead-001".equals(memberId);
    }
}
