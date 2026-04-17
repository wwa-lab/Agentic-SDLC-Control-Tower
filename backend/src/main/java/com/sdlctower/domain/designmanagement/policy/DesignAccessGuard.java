package com.sdlctower.domain.designmanagement.policy;

import com.sdlctower.domain.projectmanagement.policy.ProjectManagementActorResolver;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog.ProjectSeed;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog.RoleSeed;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import org.springframework.stereotype.Component;

@Component
public class DesignAccessGuard {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final TeamSpaceSeedCatalog teamSpaceSeedCatalog;
    private final ProjectManagementActorResolver actorResolver;

    public DesignAccessGuard(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            TeamSpaceSeedCatalog teamSpaceSeedCatalog,
            ProjectManagementActorResolver actorResolver
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.teamSpaceSeedCatalog = teamSpaceSeedCatalog;
        this.actorResolver = actorResolver;
    }

    public void requireWorkspaceRead(String workspaceId) {
        teamSpaceSeedCatalog.workspace(workspaceId);
        ProjectManagementActorResolver.Actor actor = actorResolver.currentActor();
        if ("ws-private-001".equals(workspaceId) && !actor.isElevated()) {
            throw DesignManagementException.forbidden("DM_WORKSPACE_FORBIDDEN", "Workspace access denied: " + workspaceId);
        }
    }

    public void requireRead(String projectId) {
        ProjectSeed project = projectSeedCatalog.project(projectId);
        requireWorkspaceRead(project.workspaceId());
    }

    public void requireAdmin(String projectId) {
        ProjectSeed project = projectSeedCatalog.project(projectId);
        ProjectManagementActorResolver.Actor actor = actorResolver.currentActor();
        requireWorkspaceRead(project.workspaceId());
        if (actor.isElevated()) {
            return;
        }
        boolean allowed = project.roles().stream()
                .filter(role -> "PM".equals(role.role()) || "ARCHITECT".equals(role.role()))
                .map(RoleSeed::memberId)
                .anyMatch(memberId -> memberId != null && memberId.equals(actor.memberId()));
        if (!allowed) {
            throw DesignManagementException.forbidden(
                    "DM_ROLE_REQUIRED",
                    "Design admin requires PM or Architect role on project " + projectId
            );
        }
    }

    public ProjectManagementActorResolver.Actor currentActor() {
        return actorResolver.currentActor();
    }
}
