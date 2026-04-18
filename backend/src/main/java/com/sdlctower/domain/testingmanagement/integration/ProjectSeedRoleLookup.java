package com.sdlctower.domain.testingmanagement.integration;

import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("testingManagementProjectSeedRoleLookup")
public class ProjectSeedRoleLookup implements ProjectRoleLookup {

    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;

    public ProjectSeedRoleLookup(ProjectSpaceSeedCatalog projectSpaceSeedCatalog) {
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
    }

    @Override
    public List<String> rolesForProject(String projectId, String memberId) {
        return projectSpaceSeedCatalog.project(projectId).roles().stream()
                .filter(role -> memberId.equals(role.memberId()))
                .map(ProjectSpaceSeedCatalog.RoleSeed::role)
                .toList();
    }
}
