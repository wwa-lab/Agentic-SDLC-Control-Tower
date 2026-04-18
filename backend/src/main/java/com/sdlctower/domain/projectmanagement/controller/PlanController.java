package com.sdlctower.domain.projectmanagement.controller;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.ArchiveCommandRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CreateDependencyRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CreateMilestoneRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CreateRiskRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CounterSignDependencyRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.DismissAiSuggestionRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.InternalCreateAiSuggestionRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.SaveCapacityBatchRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.TransitionDependencyRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.TransitionMilestoneRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.TransitionRiskRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.UpdateDependencyRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.UpdateMilestoneRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.UpdateRiskRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.AiSuggestionActionResultDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.AiSuggestionDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.ChangeLogPageDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.DependencyDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.MilestoneDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanAggregateDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanCapacityMatrixDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanHeaderDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.ProgressNodeDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.RiskDto;
import com.sdlctower.domain.projectmanagement.policy.PlanPolicy;
import com.sdlctower.domain.projectmanagement.service.AiSuggestionService;
import com.sdlctower.domain.projectmanagement.service.CapacityCommandService;
import com.sdlctower.domain.projectmanagement.service.DependencyCommandService;
import com.sdlctower.domain.projectmanagement.service.MilestoneCommandService;
import com.sdlctower.domain.projectmanagement.service.PlanService;
import com.sdlctower.domain.projectmanagement.service.RiskCommandService;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping(ApiConstants.PROJECT_MANAGEMENT)
public class PlanController {

    private final PlanService planService;
    private final MilestoneCommandService milestoneCommandService;
    private final CapacityCommandService capacityCommandService;
    private final RiskCommandService riskCommandService;
    private final DependencyCommandService dependencyCommandService;
    private final AiSuggestionService aiSuggestionService;
    private final PlanPolicy planPolicy;

    public PlanController(
            PlanService planService,
            MilestoneCommandService milestoneCommandService,
            CapacityCommandService capacityCommandService,
            RiskCommandService riskCommandService,
            DependencyCommandService dependencyCommandService,
            AiSuggestionService aiSuggestionService,
            PlanPolicy planPolicy
    ) {
        this.planService = planService;
        this.milestoneCommandService = milestoneCommandService;
        this.capacityCommandService = capacityCommandService;
        this.riskCommandService = riskCommandService;
        this.dependencyCommandService = dependencyCommandService;
        this.aiSuggestionService = aiSuggestionService;
        this.planPolicy = planPolicy;
    }

    @GetMapping("/plan/{projectId}")
    public ApiResponse<PlanAggregateDto> aggregate(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(planService.loadAggregate(projectId));
    }

    @GetMapping("/plan/{projectId}/header")
    public ApiResponse<PlanHeaderDto> header(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(planService.loadHeader(projectId));
    }

    @GetMapping("/plan/{projectId}/milestones")
    public ApiResponse<List<MilestoneDto>> milestones(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @RequestParam(defaultValue = "false") boolean includeArchived
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(planService.loadMilestones(projectId, includeArchived));
    }

    @GetMapping("/plan/{projectId}/capacity")
    public ApiResponse<PlanCapacityMatrixDto> capacity(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(planService.loadCapacity(projectId));
    }

    @GetMapping("/plan/{projectId}/risks")
    public ApiResponse<List<RiskDto>> risks(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String severity
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(planService.loadRisks(projectId, state, severity));
    }

    @GetMapping("/plan/{projectId}/dependencies")
    public ApiResponse<List<DependencyDto>> dependencies(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @RequestParam(required = false) String state
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(planService.loadDependencies(projectId, state));
    }

    @GetMapping("/plan/{projectId}/progress")
    public ApiResponse<List<ProgressNodeDto>> progress(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(planService.loadProgress(projectId));
    }

    @GetMapping("/plan/{projectId}/change-log")
    public ApiResponse<ChangeLogPageDto> changeLog(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @RequestParam(required = false) String actorType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(planService.loadChangeLog(projectId, actorType, targetType, from, to, page, pageSize));
    }

    @GetMapping("/plan/{projectId}/ai-suggestions")
    public ApiResponse<List<AiSuggestionDto>> aiSuggestions(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @RequestParam(defaultValue = "PENDING") String state
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(planService.loadAiSuggestions(projectId, state));
    }

    @PostMapping("/plan/{projectId}/milestones")
    public ResponseEntity<ApiResponse<MilestoneDto>> createMilestone(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @Valid @RequestBody CreateMilestoneRequest request
    ) {
        planPolicy.requireWrite(projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(milestoneCommandService.create(projectId, request)));
    }

    @PatchMapping("/plan/{projectId}/milestones/{id}")
    public ApiResponse<MilestoneDto> updateMilestone(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id,
            @Valid @RequestBody UpdateMilestoneRequest request
    ) {
        planPolicy.requireWrite(projectId);
        return ApiResponse.ok(milestoneCommandService.update(projectId, id, request));
    }

    @PostMapping("/plan/{projectId}/milestones/{id}/transition")
    public ApiResponse<MilestoneDto> transitionMilestone(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id,
            @Valid @RequestBody TransitionMilestoneRequest request
    ) {
        planPolicy.requireWrite(projectId);
        return ApiResponse.ok(milestoneCommandService.transition(projectId, id, request));
    }

    @PostMapping("/plan/{projectId}/milestones/{id}/archive")
    public ApiResponse<MilestoneDto> archiveMilestone(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id,
            @RequestBody(required = false) ArchiveCommandRequest request
    ) {
        planPolicy.requireWrite(projectId);
        return ApiResponse.ok(milestoneCommandService.archive(projectId, id, request));
    }

    @PatchMapping("/plan/{projectId}/capacity")
    public ApiResponse<PlanCapacityMatrixDto> saveCapacity(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @Valid @RequestBody SaveCapacityBatchRequest request
    ) {
        planPolicy.requireWrite(projectId);
        return ApiResponse.ok(capacityCommandService.saveBatch(projectId, request));
    }

    @PostMapping("/plan/{projectId}/risks")
    public ResponseEntity<ApiResponse<RiskDto>> createRisk(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @Valid @RequestBody CreateRiskRequest request
    ) {
        planPolicy.requireContribution(projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(riskCommandService.create(projectId, request)));
    }

    @PatchMapping("/plan/{projectId}/risks/{id}")
    public ApiResponse<RiskDto> updateRisk(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id,
            @Valid @RequestBody UpdateRiskRequest request
    ) {
        planPolicy.requireWrite(projectId);
        return ApiResponse.ok(riskCommandService.update(projectId, id, request));
    }

    @PostMapping("/plan/{projectId}/risks/{id}/transition")
    public ApiResponse<RiskDto> transitionRisk(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id,
            @Valid @RequestBody TransitionRiskRequest request
    ) {
        if ("ESCALATED".equalsIgnoreCase(request.to())) {
            planPolicy.requireContribution(projectId);
        } else {
            planPolicy.requireWrite(projectId);
        }
        return ApiResponse.ok(riskCommandService.transition(projectId, id, request));
    }

    @PostMapping("/plan/{projectId}/dependencies")
    public ResponseEntity<ApiResponse<DependencyDto>> createDependency(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @Valid @RequestBody CreateDependencyRequest request
    ) {
        planPolicy.requireContribution(projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dependencyCommandService.create(projectId, request)));
    }

    @PatchMapping("/plan/{projectId}/dependencies/{id}")
    public ApiResponse<DependencyDto> updateDependency(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id,
            @Valid @RequestBody UpdateDependencyRequest request
    ) {
        planPolicy.requireWrite(projectId);
        return ApiResponse.ok(dependencyCommandService.update(projectId, id, request));
    }

    @PostMapping("/plan/{projectId}/dependencies/{id}/transition")
    public ApiResponse<DependencyDto> transitionDependency(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id,
            @Valid @RequestBody TransitionDependencyRequest request
    ) {
        planPolicy.requireWrite(projectId);
        return ApiResponse.ok(dependencyCommandService.transition(projectId, id, request));
    }

    @PostMapping("/plan/{projectId}/dependencies/{id}/countersign")
    public ApiResponse<DependencyDto> counterSignDependency(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id,
            @Valid @RequestBody CounterSignDependencyRequest request
    ) {
        planPolicy.requireRead(projectId);
        return ApiResponse.ok(dependencyCommandService.counterSign(projectId, id, request));
    }

    @PostMapping("/plan/{projectId}/ai-suggestions/{id}/accept")
    public ApiResponse<AiSuggestionActionResultDto> acceptAiSuggestion(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id
    ) {
        planPolicy.requireWrite(projectId);
        return ApiResponse.ok(aiSuggestionService.accept(projectId, id));
    }

    @PostMapping("/plan/{projectId}/ai-suggestions/{id}/dismiss")
    public ApiResponse<AiSuggestionActionResultDto> dismissAiSuggestion(
            @PathVariable @NotBlank @Pattern(regexp = "^proj-[a-z0-9\\-]+$") String projectId,
            @PathVariable String id,
            @RequestBody(required = false) DismissAiSuggestionRequest request
    ) {
        planPolicy.requireWrite(projectId);
        return ApiResponse.ok(aiSuggestionService.dismiss(projectId, id, request));
    }

    @PostMapping("/internal/ai-suggestions")
    public ResponseEntity<ApiResponse<AiSuggestionDto>> createInternalAiSuggestion(
            @Valid @RequestBody InternalCreateAiSuggestionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(aiSuggestionService.createInternal(request)));
    }
}
