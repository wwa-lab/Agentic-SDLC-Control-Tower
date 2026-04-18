package com.sdlctower.domain.designmanagement.policy;

import com.sdlctower.shared.integration.workspace.WorkspaceAutonomyLookup;
import com.sdlctower.shared.integration.workspace.WorkspaceAutonomyLookup.WorkspaceAutonomyLevel;
import org.springframework.stereotype.Component;

@Component
public class AiAutonomyPolicy {

    private final WorkspaceAutonomyLookup workspaceAutonomyLookup;

    public AiAutonomyPolicy(WorkspaceAutonomyLookup workspaceAutonomyLookup) {
        this.workspaceAutonomyLookup = workspaceAutonomyLookup;
    }

    public void requireAtLeast(String workspaceId, WorkspaceAutonomyLevel level) {
        WorkspaceAutonomyLevel current = workspaceAutonomyLookup.currentLevel(workspaceId);
        if (current.ordinal() < level.ordinal()) {
            throw DesignManagementException.invalid(
                    "DM_AI_AUTONOMY_INSUFFICIENT",
                    "Workspace " + workspaceId + " autonomy " + current + " is below required level " + level
            );
        }
    }
}
