package com.sdlctower.platform.workspace;

import com.sdlctower.platform.auth.AuthService;
import com.sdlctower.platform.auth.CurrentUserDto;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkspaceContextController {

    private final WorkspaceContextService workspaceContextService;
    private final AuthService authService;

    public WorkspaceContextController(WorkspaceContextService workspaceContextService, AuthService authService) {
        this.workspaceContextService = workspaceContextService;
        this.authService = authService;
    }

    /**
     * Workspace-scoped endpoint (/workspaces/{id}/context) and legacy demo endpoint (/workspace-context).
     * When WorkspaceContextHolder is set (scoped request), derives context from the holder.
     * Falls back to the legacy workspace_context table for the demo/guest path.
     */
    @GetMapping({ApiConstants.WORKSPACE_CONTEXT, ApiConstants.WORKSPACE_CONTEXT_LEGACY})
    public ApiResponse<WorkspaceContextDto> getWorkspaceContext(HttpServletRequest request) {
        CurrentUserDto user = authService.fromRequest(request).orElse(null);
        return ApiResponse.ok(workspaceContextService.getCurrentWorkspaceContext(user));
    }
}
