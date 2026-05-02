package com.sdlctower.platform.workspace;

/**
 * API-facing DTO for workspace context.
 * Decouples the API contract from the JPA entity.
 */
public record WorkspaceContextDto(
        String workspaceId,
        String workspace,
        String applicationId,
        String application,
        String snowGroupId,
        String snowGroup,
        String projectId,
        String project,
        String environment,
        Boolean demoMode
) {

    public static WorkspaceContextDto fromEntity(WorkspaceContext entity) {
        return new WorkspaceContextDto(
                entity.getWorkspaceId(),
                entity.getWorkspace(),
                entity.getApplicationId(),
                entity.getApplication(),
                entity.getSnowGroupId(),
                entity.getSnowGroup(),
                entity.getProjectId(),
                entity.getProject(),
                entity.getEnvironment(),
                entity.isDemoMode()
        );
    }
}
