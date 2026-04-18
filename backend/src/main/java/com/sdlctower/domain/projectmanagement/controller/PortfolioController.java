package com.sdlctower.domain.projectmanagement.controller;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.CadenceMetricDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.DependencyBottleneckDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioAggregateDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioCapacityDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioHeatmapDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioRiskConcentrationDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioSummaryDto;
import com.sdlctower.domain.projectmanagement.policy.PlanPolicy;
import com.sdlctower.domain.projectmanagement.service.PortfolioService;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiConstants.PROJECT_MANAGEMENT + "/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PlanPolicy planPolicy;

    public PortfolioController(PortfolioService portfolioService, PlanPolicy planPolicy) {
        this.portfolioService = portfolioService;
        this.planPolicy = planPolicy;
    }

    @GetMapping
    public ApiResponse<PortfolioAggregateDto> aggregate(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        planPolicy.requireWorkspaceRead(workspaceId);
        return ApiResponse.ok(portfolioService.loadAggregate(workspaceId));
    }

    @GetMapping("/summary")
    public ApiResponse<PortfolioSummaryDto> summary(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        planPolicy.requireWorkspaceRead(workspaceId);
        return ApiResponse.ok(portfolioService.loadSummary(workspaceId));
    }

    @GetMapping("/heatmap")
    public ApiResponse<PortfolioHeatmapDto> heatmap(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId,
            @RequestParam(required = false) String window
    ) {
        planPolicy.requireWorkspaceRead(workspaceId);
        return ApiResponse.ok(portfolioService.loadHeatmap(workspaceId, window));
    }

    @GetMapping("/capacity")
    public ApiResponse<PortfolioCapacityDto> capacity(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        planPolicy.requireWorkspaceRead(workspaceId);
        return ApiResponse.ok(portfolioService.loadCapacity(workspaceId));
    }

    @GetMapping("/risks")
    public ApiResponse<PortfolioRiskConcentrationDto> risks(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String category
    ) {
        planPolicy.requireWorkspaceRead(workspaceId);
        return ApiResponse.ok(portfolioService.loadRisks(workspaceId, limit, severity, category));
    }

    @GetMapping("/dependencies")
    public ApiResponse<List<DependencyBottleneckDto>> dependencies(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId,
            @RequestParam(required = false) Integer limit
    ) {
        planPolicy.requireWorkspaceRead(workspaceId);
        return ApiResponse.ok(portfolioService.loadDependencies(workspaceId, limit));
    }

    @GetMapping("/cadence")
    public ApiResponse<List<CadenceMetricDto>> cadence(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        planPolicy.requireWorkspaceRead(workspaceId);
        return ApiResponse.ok(portfolioService.loadCadence(workspaceId));
    }
}
