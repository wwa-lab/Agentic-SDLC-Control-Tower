package com.sdlctower.domain.deploymentmanagement.policy;

import org.springframework.stereotype.Component;

@Component
public class DeploymentAccessGuard {

    public void checkReadAccess(String workspaceId) {
        if (workspaceId == null || workspaceId.isBlank()) {
            throw new DeploymentException("DP_WORKSPACE_FORBIDDEN", "Workspace ID is required");
        }
    }

    public void checkAdminAccess(String workspaceId, String role) {
        checkReadAccess(workspaceId);
        if (!"PM".equals(role) && !"TECH_LEAD".equals(role)) {
            throw new DeploymentException("DP_ROLE_REQUIRED", "Only PM or Tech Lead can perform this action");
        }
    }
}
