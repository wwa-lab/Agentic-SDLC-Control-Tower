package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.ProgressNodeDto;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DeliveryProgressProjection {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;

    public DeliveryProgressProjection(ProjectSpaceSeedCatalog projectSeedCatalog) {
        this.projectSeedCatalog = projectSeedCatalog;
    }

    public List<ProgressNodeDto> load(String projectId) {
        ProjectSpaceSeedCatalog.ProjectSeed project = projectSeedCatalog.project(projectId);
        return List.of(
                node("REQUIREMENT", project.requirementCount(), Math.max(1, project.requirementCount() - 2), "/requirements?projectId=" + projectId),
                node("USER_STORY", project.storyCount(), Math.max(1, project.storyCount() - 3), "/requirements?projectId=" + projectId + "&node=story"),
                node("SPEC", project.specCount(), Math.max(1, project.specCount() + 2), "/requirements?projectId=" + projectId + "&node=spec"),
                node("ARCHITECTURE", 3, 3, "/design?projectId=" + projectId + "&node=arch"),
                node("DESIGN", 5, 4, "/design?projectId=" + projectId),
                node("TASKS", project.taskCount(), Math.max(1, project.taskCount() - 2), "/code?projectId=" + projectId + "&node=tasks"),
                node("CODE", Math.max(1, project.taskCount() - 4), Math.max(1, project.taskCount() - 2), "/code?projectId=" + projectId),
                node("TEST", Math.max(1, project.specCount() + 4), Math.max(1, project.specCount() + 5), "/testing?projectId=" + projectId),
                node("DEPLOY", project.deployCount(), Math.max(0, project.deployCount() - 1), "/deployment?projectId=" + projectId),
                node("INCIDENT", project.openIncidentCount(), Math.max(0, project.openIncidentCount() - 1), "/incidents?projectId=" + projectId),
                node("LEARNING", 2, 3, "/reports/learning?projectId=" + projectId)
        );
    }

    private ProgressNodeDto node(String node, int throughput, int priorThroughput, String deepLink) {
        boolean slipped = priorThroughput > 0 && throughput < Math.round(priorThroughput * 0.7);
        String health = throughput == 0 && "INCIDENT".equals(node)
                ? "GREEN"
                : slipped ? "YELLOW" : throughput < priorThroughput / 2 ? "RED" : "GREEN";
        return new ProgressNodeDto(node, throughput, priorThroughput, health, slipped, deepLink);
    }
}
