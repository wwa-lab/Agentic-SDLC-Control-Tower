package com.sdlctower.domain.designmanagement.controller;

import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.LinkSpecsRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.PublishVersionRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.RegenerateAiSummaryRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.RegisterArtifactRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.RetireArtifactRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.UnlinkSpecRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.UpdateMetadataRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.AiSummaryPayloadDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.ArtifactHeaderDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.ArtifactVersionRefDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CatalogAggregateDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CatalogFiltersDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CatalogSectionDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CatalogSummaryDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.ChangeLogEntryDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.LinkedSpecRowDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.MutationResultDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.TraceabilityAggregateDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.TraceabilityGapDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.TraceabilityMatrixRowDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.TraceabilitySummaryDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.ViewerAggregateDto;
import com.sdlctower.domain.designmanagement.service.AiSummaryService;
import com.sdlctower.domain.designmanagement.service.ArtifactCommandService;
import com.sdlctower.domain.designmanagement.service.CatalogService;
import com.sdlctower.domain.designmanagement.service.SpecLinkCommandService;
import com.sdlctower.domain.designmanagement.service.TraceabilityService;
import com.sdlctower.domain.designmanagement.service.ViewerService;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiConstants.DESIGN_MANAGEMENT)
public class DesignManagementController {

    private final CatalogService catalogService;
    private final ViewerService viewerService;
    private final TraceabilityService traceabilityService;
    private final ArtifactCommandService artifactCommandService;
    private final SpecLinkCommandService specLinkCommandService;
    private final AiSummaryService aiSummaryService;

    public DesignManagementController(
            CatalogService catalogService,
            ViewerService viewerService,
            TraceabilityService traceabilityService,
            ArtifactCommandService artifactCommandService,
            SpecLinkCommandService specLinkCommandService,
            AiSummaryService aiSummaryService
    ) {
        this.catalogService = catalogService;
        this.viewerService = viewerService;
        this.traceabilityService = traceabilityService;
        this.artifactCommandService = artifactCommandService;
        this.specLinkCommandService = specLinkCommandService;
        this.aiSummaryService = aiSummaryService;
    }

    @GetMapping("/catalog")
    public ApiResponse<CatalogAggregateDto> catalog(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        return ApiResponse.ok(catalogService.loadAggregate(workspaceId));
    }

    @GetMapping("/catalog/summary")
    public ApiResponse<CatalogSummaryDto> catalogSummary(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        return ApiResponse.ok(catalogService.loadSummary(workspaceId));
    }

    @GetMapping("/catalog/grid")
    public ApiResponse<List<CatalogSectionDto>> catalogGrid(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        return ApiResponse.ok(catalogService.loadGrid(workspaceId));
    }

    @GetMapping("/catalog/filters")
    public ApiResponse<CatalogFiltersDto> catalogFilters(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        return ApiResponse.ok(catalogService.loadFilters(workspaceId));
    }

    @GetMapping("/artifacts/{artifactId}")
    public ApiResponse<ViewerAggregateDto> artifact(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @RequestParam(required = false) String version
    ) {
        return ApiResponse.ok(viewerService.loadAggregate(artifactId, version));
    }

    @GetMapping("/artifacts/{artifactId}/header")
    public ApiResponse<ArtifactHeaderDto> header(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @RequestParam(required = false) String version
    ) {
        return ApiResponse.ok(viewerService.loadHeader(artifactId, version));
    }

    @GetMapping("/artifacts/{artifactId}/versions")
    public ApiResponse<List<ArtifactVersionRefDto>> versions(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId
    ) {
        return ApiResponse.ok(viewerService.loadVersions(artifactId));
    }

    @GetMapping("/artifacts/{artifactId}/linked-specs")
    public ApiResponse<List<LinkedSpecRowDto>> linkedSpecs(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId
    ) {
        return ApiResponse.ok(viewerService.loadLinkedSpecs(artifactId));
    }

    @GetMapping("/artifacts/{artifactId}/ai-summary")
    public ApiResponse<AiSummaryPayloadDto> aiSummary(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @RequestParam(required = false) String version
    ) {
        return ApiResponse.ok(viewerService.loadAiSummary(artifactId, version));
    }

    @GetMapping("/artifacts/{artifactId}/change-log")
    public ApiResponse<List<ChangeLogEntryDto>> changeLog(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId
    ) {
        return ApiResponse.ok(viewerService.loadChangeLog(artifactId));
    }

    @GetMapping(value = "/artifacts/{artifactId}/raw", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> raw(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @RequestParam(required = false) String version
    ) {
        return ResponseEntity.ok()
                .header("Content-Security-Policy", "sandbox allow-scripts allow-same-origin; default-src 'self' 'unsafe-inline' cdn.tailwindcss.com fonts.googleapis.com fonts.gstatic.com")
                .header("X-Frame-Options", "SAMEORIGIN")
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofMinutes(5)).cachePrivate())
                .body(viewerService.loadRawPayload(artifactId, version));
    }

    @GetMapping("/traceability")
    public ApiResponse<TraceabilityAggregateDto> traceability(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        return ApiResponse.ok(traceabilityService.loadAggregate(workspaceId));
    }

    @GetMapping("/traceability/matrix")
    public ApiResponse<List<TraceabilityMatrixRowDto>> traceabilityMatrix(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        return ApiResponse.ok(traceabilityService.loadMatrix(workspaceId));
    }

    @GetMapping("/traceability/summary")
    public ApiResponse<TraceabilitySummaryDto> traceabilitySummary(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        return ApiResponse.ok(traceabilityService.loadSummary(workspaceId));
    }

    @GetMapping("/traceability/gaps")
    public ApiResponse<List<TraceabilityGapDto>> traceabilityGaps(
            @RequestParam @NotBlank @Pattern(regexp = "^ws-[a-z0-9\\-]+$") String workspaceId
    ) {
        return ApiResponse.ok(traceabilityService.loadGaps(workspaceId));
    }

    @PostMapping("/artifacts")
    public ApiResponse<MutationResultDto> register(@Valid @RequestBody RegisterArtifactRequest request) {
        return ApiResponse.ok(artifactCommandService.register(request));
    }

    @PostMapping("/artifacts/{artifactId}/versions")
    public ApiResponse<MutationResultDto> publishVersion(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @Valid @RequestBody PublishVersionRequest request
    ) {
        return ApiResponse.ok(artifactCommandService.publishVersion(artifactId, request));
    }

    @PostMapping("/artifacts/{artifactId}/retire")
    public ApiResponse<MutationResultDto> retire(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @Valid @RequestBody RetireArtifactRequest request
    ) {
        return ApiResponse.ok(artifactCommandService.retire(artifactId, request));
    }

    @PatchMapping("/artifacts/{artifactId}")
    public ApiResponse<MutationResultDto> updateMetadata(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @Valid @RequestBody UpdateMetadataRequest request
    ) {
        return ApiResponse.ok(artifactCommandService.updateMetadata(artifactId, request));
    }

    @PostMapping("/artifacts/{artifactId}/spec-links")
    public ApiResponse<MutationResultDto> linkSpecs(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @Valid @RequestBody LinkSpecsRequest request
    ) {
        return ApiResponse.ok(specLinkCommandService.linkSpecs(artifactId, request));
    }

    @DeleteMapping("/artifacts/{artifactId}/spec-links/{specId}")
    public ApiResponse<MutationResultDto> unlinkSpec(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @PathVariable @Pattern(regexp = "^[A-Za-z0-9\\-]+$") String specId,
            @Valid @RequestBody UnlinkSpecRequest request
    ) {
        return ApiResponse.ok(specLinkCommandService.unlinkSpec(artifactId, specId, request));
    }

    @PostMapping("/artifacts/{artifactId}/ai-summary/regenerate")
    public ApiResponse<MutationResultDto> regenerateAiSummary(
            @PathVariable @Pattern(regexp = "^art-[a-z0-9\\-]+$") String artifactId,
            @Valid @RequestBody(required = false) RegenerateAiSummaryRequest request
    ) {
        return ApiResponse.ok(aiSummaryService.regenerate(
                artifactId,
                request == null ? new RegenerateAiSummaryRequest(null) : request
        ));
    }
}
