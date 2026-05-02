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
import com.sdlctower.platform.workspace.WorkspaceContextHolder;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
    public ApiResponse<TeamSpaceAggregateDto> aggregate() {
        String wsId = workspaceId();
        return ApiResponse.ok(service.loadAggregate(wsId));
    }

    @GetMapping("/summary")
    public ApiResponse<WorkspaceSummaryDto> summary() {
        String wsId = workspaceId();
        return ApiResponse.ok(service.loadSummary(wsId));
    }

    @GetMapping("/operating-model")
    public ApiResponse<TeamOperatingModelDto> operatingModel() {
        String wsId = workspaceId();
        return ApiResponse.ok(service.loadOperatingModel(wsId));
    }

    @GetMapping("/members")
    public ApiResponse<MemberMatrixDto> members() {
        String wsId = workspaceId();
        return ApiResponse.ok(service.loadMembers(wsId));
    }

    @GetMapping("/templates")
    public ApiResponse<TeamDefaultTemplatesDto> templates() {
        String wsId = workspaceId();
        return ApiResponse.ok(service.loadTemplates(wsId));
    }

    @GetMapping("/pipeline")
    public ApiResponse<RequirementPipelineDto> pipeline() {
        String wsId = workspaceId();
        return ApiResponse.ok(service.loadPipeline(wsId));
    }

    @GetMapping("/metrics")
    public ApiResponse<TeamMetricsDto> metrics() {
        String wsId = workspaceId();
        return ApiResponse.ok(service.loadMetrics(wsId));
    }

    @GetMapping("/risks")
    public ApiResponse<TeamRiskRadarDto> risks() {
        String wsId = workspaceId();
        return ApiResponse.ok(service.loadRisks(wsId));
    }

    @GetMapping("/projects")
    public ApiResponse<ProjectDistributionDto> projects() {
        String wsId = workspaceId();
        return ApiResponse.ok(service.loadProjects(wsId));
    }

    private String workspaceId() {
        String wsId = WorkspaceContextHolder.current().workspaceId();
        accessGuard.check(wsId);
        return wsId;
    }
}
