package com.sdlctower.platform.workspace;

import com.sdlctower.platform.auth.CurrentUserDto;
import com.sdlctower.platform.auth.PlatformAuthException;
import com.sdlctower.platform.auth.ScopeDto;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceContextService {

    private final WorkspaceContextRepository repository;

    public WorkspaceContextService(WorkspaceContextRepository repository) {
        this.repository = repository;
    }

    public WorkspaceContextDto getCurrentWorkspaceContext() {
        return getCurrentWorkspaceContext(null);
    }

    public WorkspaceContextDto getCurrentWorkspaceContext(CurrentUserDto user) {
        // Workspace-scoped request: derive context from the holder (set by interceptor).
        WorkspaceContext ctx = WorkspaceContextHolder.maybeCurrent().orElse(null);
        if (ctx != null) {
            return fromHolder(ctx);
        }

        // Legacy path (/workspace-context): guest → demo context, staff → read from DB.
        if (user != null && "guest".equals(user.mode())) {
            return demoContext();
        }
        WorkspaceContextEntity entity = repository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Workspace context not found"));
        if (user != null && !hasRealDataScope(user, entity)) {
            throw new PlatformAuthException(HttpStatus.FORBIDDEN, "WORKSPACE_SCOPE_REQUIRED");
        }
        return WorkspaceContextDto.fromEntity(entity);
    }

    private WorkspaceContextDto fromHolder(WorkspaceContext ctx) {
        return new WorkspaceContextDto(
                ctx.workspaceId(),
                ctx.workspaceName(),
                ctx.applicationId(),
                ctx.applicationId(),
                ctx.snowGroupId(),
                ctx.snowGroupId(),
                null, null, null,
                ctx.demoMode()
        );
    }

    private WorkspaceContextDto demoContext() {
        return new WorkspaceContextDto(
                "demo-workspace",
                "Demo Workspace",
                "demo-application",
                "Demo Application",
                "demo-snow-group",
                "DEMO-SUPPORT",
                "demo-project",
                "Demo Project",
                "Demo",
                true
        );
    }

    private boolean hasRealDataScope(CurrentUserDto user, WorkspaceContextEntity entity) {
        if (user.scopes() == null) {
            return false;
        }
        return user.scopes().stream().anyMatch(scope -> matchesScope(scope, entity));
    }

    private boolean matchesScope(ScopeDto scope, WorkspaceContextEntity entity) {
        if (scope == null || scope.scopeType() == null || scope.scopeId() == null) {
            return false;
        }
        return switch (scope.scopeType()) {
            case "platform" -> "*".equals(scope.scopeId());
            case "application" -> scope.scopeId().equals(entity.getApplicationId());
            case "snow_group" -> scope.scopeId().equals(entity.getSnowGroupId());
            case "workspace" -> scope.scopeId().equals(entity.getWorkspaceId());
            case "project" -> scope.scopeId().equals(entity.getProjectId());
            default -> false;
        };
    }
}
