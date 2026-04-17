package com.sdlctower.domain.projectmanagement.policy;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog.ProjectSeed;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog.RoleSeed;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import org.springframework.stereotype.Component;

@Component
public class PlanPolicy {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final TeamSpaceSeedCatalog teamSpaceSeedCatalog;
    private final ProjectManagementActorResolver actorResolver;

    public PlanPolicy(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            TeamSpaceSeedCatalog teamSpaceSeedCatalog,
            ProjectManagementActorResolver actorResolver
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.teamSpaceSeedCatalog = teamSpaceSeedCatalog;
        this.actorResolver = actorResolver;
    }

    public void requireWorkspaceRead(String workspaceId) {
        validateWorkspaceId(workspaceId);
        teamSpaceSeedCatalog.workspace(workspaceId);
        if ("ws-private-001".equals(workspaceId) && !actorResolver.currentActor().isElevated()) {
            throw ProjectManagementException.forbidden("PM_AUTH_FORBIDDEN", "Workspace access denied: " + workspaceId);
        }
    }

    public void requireRead(String projectId) {
        ProjectSeed project = validateProject(projectId);
        if ("ws-private-001".equals(project.workspaceId()) && !actorResolver.currentActor().isElevated()) {
            throw ProjectManagementException.forbidden("PM_AUTH_FORBIDDEN", "Project access denied: " + projectId);
        }
    }

    public void requireWrite(String projectId) {
        ProjectSeed project = validateProject(projectId);
        ProjectManagementActorResolver.Actor actor = actorResolver.currentActor();
        if ("ws-private-001".equals(project.workspaceId()) && !actor.isElevated()) {
            throw ProjectManagementException.forbidden("PM_AUTH_FORBIDDEN", "Project access denied: " + projectId);
        }
        if (actor.isElevated()) {
            return;
        }
        boolean allowed = project.roles().stream()
                .filter(role -> "PM".equals(role.role()) || "TECH_LEAD".equals(role.role()))
                .map(RoleSeed::memberId)
                .anyMatch(actor.memberId()::equals);
        if (!allowed) {
            throw ProjectManagementException.forbidden(
                    "PM_AUTH_FORBIDDEN",
                    "Write access requires PM or Tech Lead on project " + projectId
            );
        }
    }

    public void requireContribution(String projectId) {
        ProjectSeed project = validateProject(projectId);
        ProjectManagementActorResolver.Actor actor = actorResolver.currentActor();
        if ("ws-private-001".equals(project.workspaceId()) && !actor.isElevated()) {
            throw ProjectManagementException.forbidden("PM_AUTH_FORBIDDEN", "Project access denied: " + projectId);
        }
        if (actor.isElevated()) {
            return;
        }
        boolean allowed = project.roles().stream()
                .map(RoleSeed::memberId)
                .anyMatch(memberId -> memberId != null && memberId.equals(actor.memberId()));
        if (!allowed) {
            throw ProjectManagementException.forbidden(
                    "PM_AUTH_FORBIDDEN",
                    "Contribution access requires project membership on project " + projectId
            );
        }
    }

    public ProjectManagementActorResolver.Actor currentActor() {
        return actorResolver.currentActor();
    }

    private void validateWorkspaceId(String workspaceId) {
        if (!ProjectManagementConstants.WORKSPACE_ID_PATTERN.matcher(workspaceId).matches()) {
            throw ProjectManagementException.badRequest("PM_VALIDATION_ERROR", "Invalid workspaceId: " + workspaceId);
        }
    }

    private ProjectSeed validateProject(String projectId) {
        if (!ProjectManagementConstants.PROJECT_ID_PATTERN.matcher(projectId).matches()) {
            throw ProjectManagementException.badRequest("PM_VALIDATION_ERROR", "Invalid projectId: " + projectId);
        }
        return projectSeedCatalog.project(projectId);
    }
}
