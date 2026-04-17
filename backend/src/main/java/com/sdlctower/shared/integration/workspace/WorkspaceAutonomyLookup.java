package com.sdlctower.shared.integration.workspace;

public interface WorkspaceAutonomyLookup {

    WorkspaceAutonomyLevel currentLevel(String workspaceId);

    enum WorkspaceAutonomyLevel {
        DISABLED,
        OBSERVATION,
        SUPERVISED,
        AUTONOMOUS
    }
}
