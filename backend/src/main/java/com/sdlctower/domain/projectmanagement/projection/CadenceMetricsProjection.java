package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.CadenceMetricDto;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CadenceMetricsProjection {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;

    public CadenceMetricsProjection(ProjectSpaceSeedCatalog projectSeedCatalog) {
        this.projectSeedCatalog = projectSeedCatalog;
    }

    public List<CadenceMetricDto> load(String workspaceId) {
        var projects = projectSeedCatalog.projectsForWorkspace(workspaceId);
        double throughput4w = projects.stream().mapToInt(ProjectSpaceSeedCatalog.ProjectSeed::taskCount).average().orElse(0.0) / 4.0;
        double throughput12w = projects.stream().mapToInt(ProjectSpaceSeedCatalog.ProjectSeed::storyCount).average().orElse(0.0) / 3.0;
        double cycleMedian = 12.0 - Math.min(4.0, throughput4w / 3.0);
        double cycleP90 = cycleMedian + 11.0;
        double hitRate = Math.min(0.95, 0.55 + (projects.stream().mapToInt(ProjectSpaceSeedCatalog.ProjectSeed::activeSpecCount).sum() / 40.0));
        double stability = Math.max(0.08, 0.35 - (projects.stream().mapToInt(ProjectSpaceSeedCatalog.ProjectSeed::pendingApprovals).sum() / 100.0));

        return List.of(
                new CadenceMetricDto("THROUGHPUT", "4W", round(throughput4w), 1.2, "UP"),
                new CadenceMetricDto("THROUGHPUT", "12W", round(throughput12w), -0.4, "DOWN"),
                new CadenceMetricDto("CYCLE_TIME_MEDIAN", "4W", round(cycleMedian), -1.0, "DOWN"),
                new CadenceMetricDto("CYCLE_TIME_P90", "4W", round(cycleP90), 2.0, "UP"),
                new CadenceMetricDto("HIT_RATE", "4W", round(hitRate), 0.05, "UP"),
                new CadenceMetricDto("PLAN_STABILITY", "4W", round(stability), -0.02, "DOWN")
        );
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
