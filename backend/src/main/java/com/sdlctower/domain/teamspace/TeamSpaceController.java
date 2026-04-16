package com.sdlctower.domain.teamspace;

import com.sdlctower.domain.teamspace.dto.MemberMatrixDto;
import com.sdlctower.domain.teamspace.dto.ProjectDistributionDto;
import com.sdlctower.domain.teamspace.dto.RequirementPipelineDto;
import com.sdlctower.domain.teamspace.dto.TeamDefaultTemplatesDto;
import com.sdlctower.domain.teamspace.dto.TeamMetricsDto;
import com.sdlctower.domain.teamspace.dto.TeamOperatingModelDto;
import com.sdlctower.domain.teamspace.dto.TeamRiskRadarDto;
import com.sdlctower.domain.teamspace.dto.TeamSpaceAggregateDto;
import com.sdlctower.domain.teamspace.dto.WorkspaceSummaryDto;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.TEAM_SPACE)
public class TeamSpaceController {

    private final TeamSpaceService service;
    private final WorkspaceAccessGuard accessGuard;

    public TeamSpaceController(TeamSpaceService service, WorkspaceAccessGuard accessGuard) {
        this.service = service;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/{workspaceId}")
    public ApiResponse<TeamSpaceAggregateDto> aggregate(@PathVariable String workspaceId) {
        guard(workspaceId);
        return ApiResponse.ok(service.loadAggregate(workspaceId));
    }

    @GetMapping("/{workspaceId}/summary")
    public ApiResponse<WorkspaceSummaryDto> summary(@PathVariable String workspaceId) {
        guard(workspaceId);
        return ApiResponse.ok(service.loadSummary(workspaceId));
    }

    @GetMapping("/{workspaceId}/operating-model")
    public ApiResponse<TeamOperatingModelDto> operatingModel(@PathVariable String workspaceId) {
        guard(workspaceId);
        return ApiResponse.ok(service.loadOperatingModel(workspaceId));
    }

    @GetMapping("/{workspaceId}/members")
    public ApiResponse<MemberMatrixDto> members(@PathVariable String workspaceId) {
        guard(workspaceId);
        return ApiResponse.ok(service.loadMembers(workspaceId));
    }

    @GetMapping("/{workspaceId}/templates")
    public ApiResponse<TeamDefaultTemplatesDto> templates(@PathVariable String workspaceId) {
        guard(workspaceId);
        return ApiResponse.ok(service.loadTemplates(workspaceId));
    }

    @GetMapping("/{workspaceId}/pipeline")
    public ApiResponse<RequirementPipelineDto> pipeline(@PathVariable String workspaceId) {
        guard(workspaceId);
        return ApiResponse.ok(service.loadPipeline(workspaceId));
    }

    @GetMapping("/{workspaceId}/metrics")
    public ApiResponse<TeamMetricsDto> metrics(@PathVariable String workspaceId) {
        guard(workspaceId);
        return ApiResponse.ok(service.loadMetrics(workspaceId));
    }

    @GetMapping("/{workspaceId}/risks")
    public ApiResponse<TeamRiskRadarDto> risks(@PathVariable String workspaceId) {
        guard(workspaceId);
        return ApiResponse.ok(service.loadRisks(workspaceId));
    }

    @GetMapping("/{workspaceId}/projects")
    public ApiResponse<ProjectDistributionDto> projects(@PathVariable String workspaceId) {
        guard(workspaceId);
        return ApiResponse.ok(service.loadProjects(workspaceId));
    }

    private void guard(String workspaceId) {
        if (!TeamSpaceConstants.WORKSPACE_ID_PATTERN.matcher(workspaceId).matches()) {
            throw new IllegalArgumentException("Invalid workspaceId: " + workspaceId);
        }
        accessGuard.check(workspaceId);
    }
}
