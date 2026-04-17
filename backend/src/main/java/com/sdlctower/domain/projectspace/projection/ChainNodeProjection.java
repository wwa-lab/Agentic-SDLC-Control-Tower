package com.sdlctower.domain.projectspace.projection;

import com.sdlctower.domain.projectspace.ProjectSpaceProjection;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.ChainNodeHealthDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.SdlcChainDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChainNodeProjection implements ProjectSpaceProjection<SdlcChainDto> {

    private final ProjectSpaceSeedCatalog seedCatalog;

    public ChainNodeProjection(ProjectSpaceSeedCatalog seedCatalog) {
        this.seedCatalog = seedCatalog;
    }

    @Override
    public SdlcChainDto load(String projectId) {
        ProjectSpaceSeedCatalog.ProjectSeed project = seedCatalog.project(projectId);
        String workspaceQuery = "workspaceId=" + project.workspaceId();
        String incidentHealth = project.openIncidentCount() > 1 ? "RED" : project.openIncidentCount() > 0 ? "YELLOW" : "GREEN";
        String specHealth = project.specCount() > 5 ? "YELLOW" : "GREEN";

        return new SdlcChainDto(List.of(
                new ChainNodeHealthDto("REQUIREMENT", "Requirement", project.requirementCount(), "GREEN", false, "/requirements?projectId=" + projectId + "&" + workspaceQuery, true),
                new ChainNodeHealthDto("USER_STORY", "User Story", project.storyCount(), project.storyCount() > 15 ? "YELLOW" : "GREEN", false, "/requirements?projectId=" + projectId + "&node=story&" + workspaceQuery, true),
                new ChainNodeHealthDto("SPEC", "Spec", project.specCount(), specHealth, true, "/requirements?projectId=" + projectId + "&node=spec&" + workspaceQuery, true),
                new ChainNodeHealthDto("ARCHITECTURE", "Architecture", 3, "GREEN", false, "/design?projectId=" + projectId + "&node=arch&" + workspaceQuery, false),
                new ChainNodeHealthDto("DESIGN", "Design", 5, "GREEN", false, "/design?projectId=" + projectId + "&" + workspaceQuery, false),
                new ChainNodeHealthDto("TASKS", "Tasks", project.taskCount(), project.taskCount() > 30 ? "YELLOW" : "GREEN", false, "/code?projectId=" + projectId + "&node=tasks&" + workspaceQuery, false),
                new ChainNodeHealthDto("CODE", "Code & Build", null, project.openIncidentCount() > 1 ? "YELLOW" : "GREEN", false, "/code?projectId=" + projectId + "&" + workspaceQuery, false),
                new ChainNodeHealthDto("TEST", "Test", 12, project.specCount() > 0 ? "YELLOW" : "GREEN", false, "/testing?projectId=" + projectId + "&" + workspaceQuery, false),
                new ChainNodeHealthDto("DEPLOY", "Deploy", project.deployCount(), project.openIncidentCount() > 1 ? "YELLOW" : "GREEN", false, "/deployment?projectId=" + projectId + "&" + workspaceQuery, false),
                new ChainNodeHealthDto("INCIDENT", "Incident", project.openIncidentCount(), incidentHealth, false, "/incidents?projectId=" + projectId + "&" + workspaceQuery, true),
                new ChainNodeHealthDto("LEARNING", "Learning", null, "GREEN", false, "/reports/learning?projectId=" + projectId + "&" + workspaceQuery, false)
        ));
    }
}
