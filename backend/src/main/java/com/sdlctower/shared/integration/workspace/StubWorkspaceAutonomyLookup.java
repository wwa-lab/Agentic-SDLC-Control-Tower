package com.sdlctower.shared.integration.workspace;

import org.springframework.stereotype.Component;

@Component
public class StubWorkspaceAutonomyLookup implements WorkspaceAutonomyLookup {

    @Override
    public WorkspaceAutonomyLevel currentLevel(String workspaceId) {
        return switch (workspaceId) {
            case "ws-private-001" -> WorkspaceAutonomyLevel.OBSERVATION;
            case "ws-legacy-001" -> WorkspaceAutonomyLevel.DISABLED;
            case "ws-degraded-001" -> WorkspaceAutonomyLevel.SUPERVISED;
            default -> WorkspaceAutonomyLevel.AUTONOMOUS;
        };
    }
}
