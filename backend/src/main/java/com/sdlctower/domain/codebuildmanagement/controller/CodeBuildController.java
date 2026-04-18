package com.sdlctower.domain.codebuildmanagement.controller;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogFiltersDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogSectionDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogSummaryDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiPrReviewDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiTriageDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.LogExcerptDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrCheckRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrCommitRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrDetailAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrHeaderDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrReviewRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RegenerateAiPrReviewRequest;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RegenerateAiPrReviewResponse;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RegenerateAiTriageRequest;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RegenerateAiTriageResponse;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoBranchDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoAiSummaryDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoDetailAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoHeaderDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoHealthSummaryDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoRecentCommitRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoRecentPrRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoRecentRunRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RerunRunRequest;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RerunRunResponse;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunDetailAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunHeaderDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunJobRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunRerunDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunStepRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityCommitRefDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityStoryRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilitySummaryDto;
import com.sdlctower.domain.codebuildmanagement.service.CatalogService;
import com.sdlctower.domain.codebuildmanagement.service.PrDetailService;
import com.sdlctower.domain.codebuildmanagement.service.RepoDetailService;
import com.sdlctower.domain.codebuildmanagement.service.RunDetailService;
import com.sdlctower.domain.codebuildmanagement.service.TraceabilityService;
import com.sdlctower.domain.codebuildmanagement.service.AiPrReviewCommandService;
import com.sdlctower.domain.codebuildmanagement.service.AiTriageCommandService;
import com.sdlctower.domain.codebuildmanagement.service.RunRerunCommandService;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiConstants.CODE_BUILD_MANAGEMENT)
public class CodeBuildController {

    private final CatalogService catalogService;
    private final RepoDetailService repoDetailService;
    private final PrDetailService prDetailService;
    private final RunDetailService runDetailService;
    private final TraceabilityService traceabilityService;
    private final AiPrReviewCommandService aiPrReviewCommandService;
    private final AiTriageCommandService aiTriageCommandService;
    private final RunRerunCommandService runRerunCommandService;

    public CodeBuildController(
            CatalogService catalogService,
            RepoDetailService repoDetailService,
            PrDetailService prDetailService,
            RunDetailService runDetailService,
            TraceabilityService traceabilityService,
            AiPrReviewCommandService aiPrReviewCommandService,
            AiTriageCommandService aiTriageCommandService,
            RunRerunCommandService runRerunCommandService
    ) {
        this.catalogService = catalogService;
        this.repoDetailService = repoDetailService;
        this.prDetailService = prDetailService;
        this.runDetailService = runDetailService;
        this.traceabilityService = traceabilityService;
        this.aiPrReviewCommandService = aiPrReviewCommandService;
        this.aiTriageCommandService = aiTriageCommandService;
        this.runRerunCommandService = runRerunCommandService;
    }

    // ── Catalog ──

    @GetMapping("/catalog")
    public ApiResponse<CatalogAggregateDto> catalog(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String buildStatus,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.ok(catalogService.loadAggregate(workspaceId, projectId, buildStatus, visibility, search));
    }

    @GetMapping("/catalog/summary")
    public ApiResponse<CatalogSummaryDto> catalogSummary(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String buildStatus,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.ok(catalogService.loadSummary(workspaceId, projectId, buildStatus, visibility, search));
    }

    @GetMapping("/catalog/grid")
    public ApiResponse<List<CatalogSectionDto>> catalogGrid(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String buildStatus,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.ok(catalogService.loadGrid(workspaceId, projectId, buildStatus, visibility, search));
    }

    // ── Repo Detail ──

    @GetMapping("/repos/{repoId}")
    public ApiResponse<RepoDetailAggregateDto> repoAggregate(
            @PathVariable @Pattern(regexp = "^repo-[a-z0-9\\-]+$") String repoId
    ) {
        return ApiResponse.ok(repoDetailService.loadAggregate(repoId));
    }

    @GetMapping("/repos/{repoId}/header")
    public ApiResponse<RepoHeaderDto> repoHeader(
            @PathVariable @Pattern(regexp = "^repo-[a-z0-9\\-]+$") String repoId
    ) {
        return ApiResponse.ok(repoDetailService.loadHeader(repoId));
    }

    @GetMapping("/repos/{repoId}/recent-runs")
    public ApiResponse<List<RepoRecentRunRowDto>> repoRecentRuns(
            @PathVariable @Pattern(regexp = "^repo-[a-z0-9\\-]+$") String repoId
    ) {
        return ApiResponse.ok(repoDetailService.loadRecentRuns(repoId));
    }

    @GetMapping("/repos/{repoId}/recent-prs")
    public ApiResponse<List<RepoRecentPrRowDto>> repoRecentPrs(
            @PathVariable @Pattern(regexp = "^repo-[a-z0-9\\-]+$") String repoId
    ) {
        return ApiResponse.ok(repoDetailService.loadRecentPrs(repoId));
    }

    @GetMapping("/repos/{repoId}/recent-commits")
    public ApiResponse<List<RepoRecentCommitRowDto>> repoRecentCommits(
            @PathVariable @Pattern(regexp = "^repo-[a-z0-9\\-]+$") String repoId
    ) {
        return ApiResponse.ok(repoDetailService.loadRecentCommits(repoId));
    }

    @GetMapping("/repos/{repoId}/branches")
    public ApiResponse<List<RepoBranchDto>> repoBranches(
            @PathVariable @Pattern(regexp = "^repo-[a-z0-9\\-]+$") String repoId
    ) {
        return ApiResponse.ok(repoDetailService.loadBranches(repoId));
    }

    @GetMapping("/repos/{repoId}/health-summary")
    public ApiResponse<RepoHealthSummaryDto> repoHealthSummary(
            @PathVariable @Pattern(regexp = "^repo-[a-z0-9\\-]+$") String repoId
    ) {
        return ApiResponse.ok(repoDetailService.loadHealthSummary(repoId));
    }

    @GetMapping("/repos/{repoId}/ai-summary")
    public ApiResponse<RepoAiSummaryDto> repoAiSummary(
            @PathVariable @Pattern(regexp = "^repo-[a-z0-9\\-]+$") String repoId
    ) {
        return ApiResponse.ok(repoDetailService.loadAiSummary(repoId));
    }

    // ── PR Detail ──

    @GetMapping("/prs/{prId}")
    public ApiResponse<PrDetailAggregateDto> prAggregate(
            @PathVariable @Pattern(regexp = "^pr-[a-z0-9\\-]+$") String prId,
            @RequestParam(defaultValue = "mem-default-001") String memberId
    ) {
        return ApiResponse.ok(prDetailService.loadAggregate(prId, memberId));
    }

    @GetMapping("/prs/{prId}/header")
    public ApiResponse<PrHeaderDto> prHeader(
            @PathVariable @Pattern(regexp = "^pr-[a-z0-9\\-]+$") String prId
    ) {
        return ApiResponse.ok(prDetailService.loadHeader(prId));
    }

    @GetMapping("/prs/{prId}/checks")
    public ApiResponse<List<PrCheckRowDto>> prChecks(
            @PathVariable @Pattern(regexp = "^pr-[a-z0-9\\-]+$") String prId
    ) {
        return ApiResponse.ok(prDetailService.loadChecks(prId));
    }

    @GetMapping("/prs/{prId}/reviews")
    public ApiResponse<List<PrReviewRowDto>> prReviews(
            @PathVariable @Pattern(regexp = "^pr-[a-z0-9\\-]+$") String prId
    ) {
        return ApiResponse.ok(prDetailService.loadReviews(prId));
    }

    @GetMapping("/prs/{prId}/commits")
    public ApiResponse<List<PrCommitRowDto>> prCommits(
            @PathVariable @Pattern(regexp = "^pr-[a-z0-9\\-]+$") String prId
    ) {
        return ApiResponse.ok(prDetailService.loadCommits(prId));
    }

    @GetMapping("/prs/{prId}/ai-review")
    public ApiResponse<AiPrReviewDto> prAiReview(
            @PathVariable @Pattern(regexp = "^pr-[a-z0-9\\-]+$") String prId,
            @RequestParam(defaultValue = "mem-default-001") String memberId
    ) {
        return ApiResponse.ok(prDetailService.loadAiReview(prId, memberId));
    }

    // ── Run Detail ──

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

    @GetMapping("/runs/{runId}/jobs")
    public ApiResponse<List<RunJobRowDto>> runJobs(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId
    ) {
        return ApiResponse.ok(runDetailService.loadJobs(runId));
    }

    @GetMapping("/runs/{runId}/steps")
    public ApiResponse<List<RunStepRowDto>> runSteps(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId
    ) {
        return ApiResponse.ok(runDetailService.loadSteps(runId));
    }

    @GetMapping("/runs/{runId}/logs")
    public ApiResponse<String> runLogs(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId
    ) {
        return ApiResponse.ok(runDetailService.loadLogs(runId));
    }

    @GetMapping("/runs/{runId}/ai-triage")
    public ApiResponse<AiTriageDto> runAiTriage(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId
    ) {
        return ApiResponse.ok(runDetailService.loadAiTriage(runId));
    }

    @GetMapping("/runs/{runId}/rerun")
    public ApiResponse<RunRerunDto> runRerunStatus(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId
    ) {
        return ApiResponse.ok(runDetailService.loadRerunStatus(runId));
    }

    // ── Traceability ──

    @GetMapping("/traceability")
    public ApiResponse<TraceabilityAggregateDto> traceability(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String linkStatus,
            @RequestParam(required = false) String storyState
    ) {
        return ApiResponse.ok(traceabilityService.loadAggregate(workspaceId, projectId, linkStatus, storyState));
    }

    @GetMapping("/traceability/story-rows")
    public ApiResponse<List<TraceabilityStoryRowDto>> traceabilityStoryRows(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId,
            @RequestParam(required = false) String projectId
    ) {
        return ApiResponse.ok(traceabilityService.loadStoryRows(workspaceId, projectId));
    }

    @GetMapping("/traceability/unknown-story")
    public ApiResponse<List<TraceabilityCommitRefDto>> traceabilityUnknownStory(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId
    ) {
        return ApiResponse.ok(traceabilityService.loadUnknownStoryRows(workspaceId));
    }

    @GetMapping("/traceability/no-story-id")
    public ApiResponse<List<TraceabilityCommitRefDto>> traceabilityNoStoryId(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId
    ) {
        return ApiResponse.ok(traceabilityService.loadNoStoryIdRows(workspaceId));
    }

    @GetMapping("/traceability/summary")
    public ApiResponse<TraceabilitySummaryDto> traceabilitySummary(
            @RequestParam(defaultValue = "ws-default-001") String workspaceId
    ) {
        return ApiResponse.ok(traceabilityService.loadSummary(workspaceId));
    }

    // ── Mutations ──

    @PostMapping("/prs/{prId}/ai-review/regenerate")
    public ApiResponse<RegenerateAiPrReviewResponse> regenerateAiPrReview(
            @PathVariable @Pattern(regexp = "^pr-[a-z0-9\\-]+$") String prId,
            @RequestBody @Valid RegenerateAiPrReviewRequest request,
            @RequestParam(defaultValue = "mem-default-001") String memberId
    ) {
        return ApiResponse.ok(aiPrReviewCommandService.regenerate(prId, request, memberId));
    }

    @PostMapping("/runs/{runId}/ai-triage/regenerate")
    public ApiResponse<RegenerateAiTriageResponse> regenerateAiTriage(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId,
            @RequestBody @Valid RegenerateAiTriageRequest request,
            @RequestParam(defaultValue = "mem-default-001") String memberId
    ) {
        return ApiResponse.ok(aiTriageCommandService.regenerate(runId, request, memberId));
    }

    @PostMapping("/runs/{runId}/rerun")
    public ApiResponse<RerunRunResponse> rerunRun(
            @PathVariable @Pattern(regexp = "^run-[a-z0-9\\-]+$") String runId,
            @RequestBody @Valid RerunRunRequest request,
            @RequestParam(defaultValue = "mem-default-001") String memberId
    ) {
        return ApiResponse.ok(runRerunCommandService.rerun(runId, request, memberId));
    }
}
