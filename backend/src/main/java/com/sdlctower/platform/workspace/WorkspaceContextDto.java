package com.sdlctower.platform.workspace;

/**
 * API-facing DTO for workspace context.
 * Decouples the API contract from the JPA entity.
 */
public record WorkspaceContextDto(
        String workspace,
        String application,
        String snowGroup,
        String project,
        String environment
) {

    public static WorkspaceContextDto fromEntity(WorkspaceContext entity) {
        return new WorkspaceContextDto(
                entity.getWorkspace(),
                entity.getApplication(),
                entity.getSnowGroup(),
                entity.getProject(),
                entity.getEnvironment()
        );
    }
}
