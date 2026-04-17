package com.sdlctower.domain.projectmanagement.policy;

import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog.RoleSeed;
import org.springframework.stereotype.Component;

@Component
public class DependencyCounterSignPolicy {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final ProjectManagementActorResolver actorResolver;

    public DependencyCounterSignPolicy(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            ProjectManagementActorResolver actorResolver
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.actorResolver = actorResolver;
    }

    public void requireCounterSignAuthority(String targetProjectId) {
        ProjectManagementActorResolver.Actor actor = actorResolver.currentActor();
        if (actor.isElevated()) {
            return;
        }
        boolean allowed = projectSeedCatalog.project(targetProjectId).roles().stream()
                .filter(role -> "PM".equals(role.role()) || "TECH_LEAD".equals(role.role()))
                .map(RoleSeed::memberId)
                .anyMatch(actor.memberId()::equals);
        if (!allowed) {
            throw ProjectManagementException.forbidden(
                    "PM_AUTH_FORBIDDEN",
                    "Counter-sign requires PM or Tech Lead on target project " + targetProjectId
            );
        }
    }
}
