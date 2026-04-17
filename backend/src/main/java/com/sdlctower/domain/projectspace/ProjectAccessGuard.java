package com.sdlctower.domain.projectspace;

import org.springframework.stereotype.Component;

@Component
public class ProjectAccessGuard {

    private final ProjectSpaceSeedCatalog seedCatalog;

    public ProjectAccessGuard(ProjectSpaceSeedCatalog seedCatalog) {
        this.seedCatalog = seedCatalog;
    }

    public void check(String projectId) {
        if (!ProjectSpaceConstants.PROJECT_ID_PATTERN.matcher(projectId).matches()) {
            throw new IllegalArgumentException("Invalid projectId: " + projectId);
        }

        ProjectSpaceSeedCatalog.ProjectSeed project = seedCatalog.project(projectId);
        if ("ws-private-001".equals(project.workspaceId())) {
            throw new ProjectAccessDeniedException("Project access denied: " + projectId);
        }
    }
}
