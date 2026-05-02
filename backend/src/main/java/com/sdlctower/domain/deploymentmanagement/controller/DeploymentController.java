package com.sdlctower.domain.deploymentmanagement.controller;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.*;
import com.sdlctower.domain.deploymentmanagement.service.*;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping(ApiConstants.DEPLOYMENT_MANAGEMENT)
public class DeploymentController {

    private final CatalogService catalogService;
    private final ApplicationDetailService applicationDetailService;
    private final ReleaseDetailService releaseDetailService;
    private final DeployDetailService deployDetailService;
    private final EnvironmentDetailService environmentDetailService;
    private final TraceabilityService traceabilityService;
    private final AiReleaseNotesService aiReleaseNotesService;
    private final AiDeploymentSummaryService aiDeploymentSummaryService;

    public DeploymentController(CatalogService catalogService,
                                 ApplicationDetailService applicationDetailService,
                                 ReleaseDetailService releaseDetailService,
                                 DeployDetailService deployDetailService,
                                 EnvironmentDetailService environmentDetailService,
                                 TraceabilityService traceabilityService,
                                 AiReleaseNotesService aiReleaseNotesService,
                                 AiDeploymentSummaryService aiDeploymentSummaryService) {
        this.catalogService = catalogService;
        this.applicationDetailService = applicationDetailService;
        this.releaseDetailService = releaseDetailService;
        this.deployDetailService = deployDetailService;
        this.environmentDetailService = environmentDetailService;
        this.traceabilityService = traceabilityService;
        this.aiReleaseNotesService = aiReleaseNotesService;
        this.aiDeploymentSummaryService = aiDeploymentSummaryService;
    }

    @GetMapping("/catalog")
    public ResponseEntity<ApiResponse<CatalogAggregateDto>> getCatalog(
            @RequestParam(required = false) String window,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String environmentKind,
            @RequestParam(required = false) String deployStatus) {
        var filters = new CatalogFiltersDto(null, environmentKind, deployStatus, window, search);
        return ResponseEntity.ok(ApiResponse.ok(catalogService.loadAggregate(filters)));
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationDetailAggregateDto>> getApplication(
            @PathVariable @Pattern(regexp = "^app-[a-z0-9\\-]+$") String applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(applicationDetailService.loadAggregate(applicationId)));
    }

    @GetMapping("/releases/{releaseId}")
    public ResponseEntity<ApiResponse<ReleaseDetailAggregateDto>> getRelease(
            @PathVariable @Pattern(regexp = "^release-[a-z0-9\\-]+$") String releaseId) {
        return ResponseEntity.ok(ApiResponse.ok(releaseDetailService.loadAggregate(releaseId)));
    }

    @GetMapping("/deploys/{deployId}")
    public ResponseEntity<ApiResponse<DeployDetailAggregateDto>> getDeploy(
            @PathVariable @Pattern(regexp = "^deploy-[a-z0-9\\-]+$") String deployId) {
        return ResponseEntity.ok(ApiResponse.ok(deployDetailService.loadAggregate(deployId)));
    }

    @GetMapping("/applications/{applicationId}/environments/{environmentName}")
    public ResponseEntity<ApiResponse<EnvironmentDetailAggregateDto>> getEnvironment(
            @PathVariable @Pattern(regexp = "^app-[a-z0-9\\-]+$") String applicationId,
            @PathVariable @Pattern(regexp = "^[a-z][a-z0-9_\\-]{0,62}$") String environmentName) {
        return ResponseEntity.ok(ApiResponse.ok(
                environmentDetailService.loadAggregate(applicationId, environmentName)));
    }

    @GetMapping("/traceability")
    public ResponseEntity<ApiResponse<TraceabilityAggregateDto>> getTraceability(
            @RequestParam @NotBlank String storyId) {
        return ResponseEntity.ok(ApiResponse.ok(traceabilityService.inverseLookup(storyId)));
    }

    @PostMapping("/releases/{releaseId}/ai-notes/regenerate")
    public ResponseEntity<ApiResponse<RegenerateReleaseNotesResponse>> regenerateReleaseNotes(
            @PathVariable @Pattern(regexp = "^release-[a-z0-9\\-]+$") String releaseId) {
        return ResponseEntity.ok(ApiResponse.ok(aiReleaseNotesService.enqueueRegeneration(releaseId)));
    }

    @PostMapping("/deploys/{deployId}/ai-summary/regenerate")
    public ResponseEntity<ApiResponse<RegenerateDeploySummaryResponse>> regenerateDeploySummary(
            @PathVariable @Pattern(regexp = "^deploy-[a-z0-9\\-]+$") String deployId) {
        return ResponseEntity.ok(ApiResponse.ok(aiDeploymentSummaryService.enqueueRegeneration(deployId)));
    }

    @PostMapping("/ai-summary/regenerate")
    public ResponseEntity<ApiResponse<RegenerateWorkspaceSummaryResponse>> regenerateWorkspaceSummary() {
        String workspaceId = com.sdlctower.platform.workspace.WorkspaceContextHolder.current().workspaceId();
        return ResponseEntity.ok(ApiResponse.ok(
                new RegenerateWorkspaceSummaryResponse(workspaceId,
                        com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.AiRowStatus.PENDING,
                        java.time.Instant.now())));
    }
}
