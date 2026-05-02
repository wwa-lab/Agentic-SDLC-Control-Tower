package com.sdlctower.platform.workspace;

import com.sdlctower.platform.auth.AuthService;
import com.sdlctower.platform.auth.CurrentUserDto;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.WORKSPACE_CONTEXT)
public class WorkspaceContextController {

    private final WorkspaceContextService workspaceContextService;
    private final AuthService authService;

    public WorkspaceContextController(WorkspaceContextService workspaceContextService, AuthService authService) {
        this.workspaceContextService = workspaceContextService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<WorkspaceContextDto> getWorkspaceContext(HttpServletRequest request) {
        CurrentUserDto user = authService.requireUser(request);
        return ApiResponse.ok(workspaceContextService.getCurrentWorkspaceContext(user));
    }
}
