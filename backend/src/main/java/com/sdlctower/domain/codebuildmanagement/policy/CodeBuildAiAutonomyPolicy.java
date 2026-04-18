package com.sdlctower.domain.codebuildmanagement.policy;

import org.springframework.stereotype.Component;

@Component("codeBuildManagementAiAutonomyPolicy")
public class CodeBuildAiAutonomyPolicy {

    private static final String SUPERVISED = "SUPERVISED";

    public void requireAtLeast(String workspaceId, String requiredLevel) {
        if (workspaceId == null || workspaceId.isBlank()) {
            throw CodeBuildManagementException.badRequest(
                    "CB_VALIDATION_ERROR", "workspaceId is required");
        }
        if (requiredLevel == null || requiredLevel.isBlank()) {
            throw CodeBuildManagementException.badRequest(
                    "CB_VALIDATION_ERROR", "requiredLevel is required");
        }

        // V1 implementation: all workspaces are SUPERVISED level.
        // Only SUPERVISED level passes; anything higher is rejected.
        if (!SUPERVISED.equalsIgnoreCase(requiredLevel)) {
            throw CodeBuildManagementException.conflict(
                    "CB_AI_AUTONOMY_INSUFFICIENT",
                    "Workspace " + workspaceId + " autonomy level is " + SUPERVISED
                            + " but " + requiredLevel + " is required");
        }
    }
}
