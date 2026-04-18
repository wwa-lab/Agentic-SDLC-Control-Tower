package com.sdlctower.domain.projectmanagement.projection;

import java.util.Collection;
import java.util.Comparator;

final class ProjectManagementProjectionSupport {

    private ProjectManagementProjectionSupport() {}

    static int severityRank(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    static int milestoneRank(String status) {
        return switch (status) {
            case "SLIPPED" -> 6;
            case "AT_RISK" -> 5;
            case "IN_PROGRESS" -> 4;
            case "NOT_STARTED" -> 3;
            case "COMPLETED" -> 2;
            case "ARCHIVED" -> 1;
            default -> 0;
        };
    }

    static String dominantMilestoneStatus(Collection<String> statuses) {
        return statuses.stream()
                .max(Comparator.comparingInt(ProjectManagementProjectionSupport::milestoneRank))
                .orElse("NONE");
    }

    static String planHealth(boolean hasSlipped, boolean hasCriticalRisk, boolean hasRedDependency, boolean hasYellowSignal) {
        if (hasSlipped || hasCriticalRisk || hasRedDependency) {
            return "RED";
        }
        if (hasYellowSignal) {
            return "YELLOW";
        }
        return "GREEN";
    }
}
