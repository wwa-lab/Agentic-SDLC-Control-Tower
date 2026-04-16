package com.sdlctower.platform.profile;

import com.sdlctower.platform.workspace.WorkspaceContextDto;
import com.sdlctower.platform.workspace.WorkspaceContextService;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PipelineProfileController {

    private final PipelineProfileService pipelineProfileService;
    private final WorkspaceContextService workspaceContextService;

    public PipelineProfileController(
            PipelineProfileService pipelineProfileService,
            WorkspaceContextService workspaceContextService
    ) {
        this.pipelineProfileService = pipelineProfileService;
        this.workspaceContextService = workspaceContextService;
    }

    @GetMapping(ApiConstants.PIPELINE_PROFILES_ACTIVE)
    public ApiResponse<PipelineProfileDto> getActiveProfile() {
        WorkspaceContextDto workspaceContext = workspaceContextService.getCurrentWorkspaceContext();
        return ApiResponse.ok(pipelineProfileService.getActiveProfile(
                workspaceContext.workspace(),
                workspaceContext.application(),
                workspaceContext.project()));
    }
}
