package com.sdlctower.domain.projectspace;

import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.DependencyMapDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.EnvironmentMatrixDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.LeadershipOwnershipDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.MilestoneHubDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.ProjectSpaceAggregateDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.ProjectSummaryDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.RiskRegistryDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.SdlcChainDto;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.PROJECT_SPACE)
public class ProjectSpaceController {

    private final ProjectSpaceService service;
    private final ProjectAccessGuard accessGuard;

    public ProjectSpaceController(ProjectSpaceService service, ProjectAccessGuard accessGuard) {
        this.service = service;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectSpaceAggregateDto> aggregate(@PathVariable String projectId) {
        guard(projectId);
        return ApiResponse.ok(service.loadAggregate(projectId));
    }

    @GetMapping("/{projectId}/summary")
    public ApiResponse<ProjectSummaryDto> summary(@PathVariable String projectId) {
        guard(projectId);
        return ApiResponse.ok(service.loadSummary(projectId));
    }

    @GetMapping("/{projectId}/leadership")
    public ApiResponse<LeadershipOwnershipDto> leadership(@PathVariable String projectId) {
        guard(projectId);
        return ApiResponse.ok(service.loadLeadership(projectId));
    }

    @GetMapping("/{projectId}/chain")
    public ApiResponse<SdlcChainDto> chain(@PathVariable String projectId) {
        guard(projectId);
        return ApiResponse.ok(service.loadChain(projectId));
    }

    @GetMapping("/{projectId}/milestones")
    public ApiResponse<MilestoneHubDto> milestones(@PathVariable String projectId) {
        guard(projectId);
        return ApiResponse.ok(service.loadMilestones(projectId));
    }

    @GetMapping("/{projectId}/dependencies")
    public ApiResponse<DependencyMapDto> dependencies(@PathVariable String projectId) {
        guard(projectId);
        return ApiResponse.ok(service.loadDependencies(projectId));
    }

    @GetMapping("/{projectId}/risks")
    public ApiResponse<RiskRegistryDto> risks(@PathVariable String projectId) {
        guard(projectId);
        return ApiResponse.ok(service.loadRisks(projectId));
    }

    @GetMapping("/{projectId}/environments")
    public ApiResponse<EnvironmentMatrixDto> environments(@PathVariable String projectId) {
        guard(projectId);
        return ApiResponse.ok(service.loadEnvironments(projectId));
    }

    private void guard(String projectId) {
        if (!ProjectSpaceConstants.PROJECT_ID_PATTERN.matcher(projectId).matches()) {
            throw new IllegalArgumentException("Invalid projectId: " + projectId);
        }
        accessGuard.check(projectId);
    }
}
