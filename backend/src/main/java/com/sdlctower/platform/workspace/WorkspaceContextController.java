package com.sdlctower.platform.workspace;

import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.WORKSPACE_CONTEXT)
public class WorkspaceContextController {

    private final WorkspaceContextService workspaceContextService;

    public WorkspaceContextController(WorkspaceContextService workspaceContextService) {
        this.workspaceContextService = workspaceContextService;
    }

    @GetMapping
    public ApiResponse<WorkspaceContextDto> getWorkspaceContext() {
        return ApiResponse.ok(workspaceContextService.getCurrentWorkspaceContext());
    }
}
