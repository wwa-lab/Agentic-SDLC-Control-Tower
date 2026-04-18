package com.sdlctower.domain.testingmanagement.controller;

import com.sdlctower.domain.testingmanagement.TestingManagementConstants;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CaseDetailAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogFiltersDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogPlanRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogSummaryDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.PlanCaseRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.PlanDetailAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.PlanHeaderDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RecentRunRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunCaseResultRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunCoverageDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunDetailAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunHeaderDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.TraceabilityAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.TraceabilityReqRowDto;
import com.sdlctower.domain.testingmanagement.service.CaseDetailService;
import com.sdlctower.domain.testingmanagement.service.CatalogService;
import com.sdlctower.domain.testingmanagement.service.PlanDetailService;
import com.sdlctower.domain.testingmanagement.service.RunDetailService;
import com.sdlctower.domain.testingmanagement.service.TraceabilityService;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping({ApiConstants.TESTING, ApiConstants.TESTING_MANAGEMENT})
public class TestingManagementController {

    private final CatalogService catalogService;
    private final PlanDetailService planDetailService;
    private final CaseDetailService caseDetailService;
    private final RunDetailService runDetailService;
    private final TraceabilityService traceabilityService;

    public TestingManagementController(
            CatalogService catalogService,
            PlanDetailService planDetailService,
            CaseDetailService caseDetailService,
            RunDetailService runDetailService,
            TraceabilityService traceabilityService
    ) {
        this.catalogService = catalogService;
        this.planDetailService = planDetailService;
        this.caseDetailService = caseDetailService;
        this.runDetailService = runDetailService;
        this.traceabilityService = traceabilityService;
    }

    @GetMapping("/catalog")
    public ApiResponse<CatalogAggregateDto> catalog(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String planState,
            @RequestParam(required = false) String coverageLed,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.ok(catalogService.loadAggregate(workspaceId, projectId, planState, coverageLed, search));
    }

    @GetMapping("/catalog/summary")
    public ApiResponse<CatalogSummaryDto> catalogSummary(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String planState,
            @RequestParam(required = false) String coverageLed,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.ok(catalogService.loadSummary(workspaceId, projectId, planState, coverageLed, search));
    }

    @GetMapping("/catalog/grid")
    public ApiResponse<List<CatalogPlanRowDto>> catalogGrid(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String planState,
            @RequestParam(required = false) String coverageLed,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.ok(catalogService.loadGrid(workspaceId, projectId, planState, coverageLed, search));
    }

    @GetMapping("/catalog/filters")
    public ApiResponse<CatalogFiltersDto> catalogFilters(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId
    ) {
        return ApiResponse.ok(catalogService.loadFilters(workspaceId));
    }

    @GetMapping("/plans/{planId}")
    public ApiResponse<PlanDetailAggregateDto> planAggregate(
            @PathVariable @Pattern(regexp = "^plan-[a-z0-9\\-]+$") String planId
    ) {
        return ApiResponse.ok(planDetailService.loadAggregate(planId));
    }

    @GetMapping("/plans/{planId}/header")
    public ApiResponse<PlanHeaderDto> planHeader(
            @PathVariable @Pattern(regexp = "^plan-[a-z0-9\\-]+$") String planId
    ) {
        return ApiResponse.ok(planDetailService.loadHeader(planId));
    }

    @GetMapping("/plans/{planId}/cases")
    public ApiResponse<List<PlanCaseRowDto>> planCases(
            @PathVariable @Pattern(regexp = "^plan-[a-z0-9\\-]+$") String planId
    ) {
        return ApiResponse.ok(planDetailService.loadCases(planId));
    }

    @GetMapping("/plans/{planId}/coverage")
    public ApiResponse<List<com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CoverageRowDto>> planCoverage(
            @PathVariable @Pattern(regexp = "^plan-[a-z0-9\\-]+$") String planId
    ) {
        return ApiResponse.ok(planDetailService.loadCoverage(planId));
    }

    @GetMapping("/plans/{planId}/recent-runs")
    public ApiResponse<List<RecentRunRowDto>> planRecentRuns(
            @PathVariable @Pattern(regexp = "^plan-[a-z0-9\\-]+$") String planId
    ) {
        return ApiResponse.ok(planDetailService.loadRecentRuns(planId));
    }

    @GetMapping("/plans/{planId}/draft-inbox")
    public ApiResponse<List<com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.AiDraftRowDto>> planDraftInbox(
            @PathVariable @Pattern(regexp = "^plan-[a-z0-9\\-]+$") String planId
    ) {
        return ApiResponse.ok(planDetailService.loadDraftInbox(planId));
    }

    @GetMapping("/plans/{planId}/ai-insights")
    public ApiResponse<com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.AiInsightsDto> planAiInsights(
            @PathVariable @Pattern(regexp = "^plan-[a-z0-9\\-]+$") String planId
    ) {
        return ApiResponse.ok(planDetailService.loadAiInsights(planId));
    }

    @GetMapping("/cases/{caseId}")
    public ApiResponse<CaseDetailAggregateDto> caseAggregate(
            @PathVariable @Pattern(regexp = "^case-[a-z0-9\\-]+$") String caseId
    ) {
        return ApiResponse.ok(caseDetailService.load(caseId));
    }

    @GetMapping("/runs/{runId}")
    public ApiResponse<RunDetailAggregateDto> runAggregate(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId
    ) {
        return ApiResponse.ok(runDetailService.loadAggregate(runId));
    }

    @GetMapping("/runs/{runId}/header")
    public ApiResponse<RunHeaderDto> runHeader(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId
    ) {
        return ApiResponse.ok(runDetailService.loadHeader(runId));
    }

    @GetMapping("/runs/{runId}/case-results")
    public ApiResponse<List<RunCaseResultRowDto>> runCaseResults(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId
    ) {
        return ApiResponse.ok(runDetailService.loadCaseResults(runId));
    }

    @GetMapping("/runs/{runId}/coverage")
    public ApiResponse<RunCoverageDto> runCoverage(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId
    ) {
        return ApiResponse.ok(runDetailService.loadCoverage(runId));
    }

    @GetMapping("/traceability")
    public ApiResponse<TraceabilityAggregateDto> traceability(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId
    ) {
        return ApiResponse.ok(traceabilityService.loadAggregate(workspaceId));
    }

    @GetMapping("/traceability/req-rows")
    public ApiResponse<List<TraceabilityReqRowDto>> traceabilityReqRows(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId
    ) {
        return ApiResponse.ok(traceabilityService.loadReqRows(workspaceId));
    }
}
