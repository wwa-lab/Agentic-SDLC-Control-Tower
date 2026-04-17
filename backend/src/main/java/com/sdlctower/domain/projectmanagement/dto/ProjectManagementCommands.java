package com.sdlctower.domain.projectmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public final class ProjectManagementCommands {

    private ProjectManagementCommands() {}

    public record CreateMilestoneRequest(
            @NotBlank String label,
            String description,
            @NotNull LocalDate targetDate,
            String ownerMemberId,
            @NotNull Integer ordering
    ) {}

    public record UpdateMilestoneRequest(
            String label,
            String description,
            LocalDate targetDate,
            String ownerMemberId,
            Integer ordering,
            @NotNull Long planRevision
    ) {}

    public record TransitionMilestoneRequest(
            @NotBlank String to,
            String slippageReason,
            LocalDate newTargetDate,
            @NotNull Long planRevision
    ) {}

    public record ArchiveCommandRequest(Long planRevision) {}

    public record CapacityEditRequest(
            @NotBlank String memberId,
            @NotBlank String milestoneId,
            @Min(0) @Max(500) int percent,
            String justification,
            @NotNull Long planRevision
    ) {}

    public record SaveCapacityBatchRequest(
            @Valid @NotEmpty List<CapacityEditRequest> edits
    ) {}

    public record CreateRiskRequest(
            @NotBlank String title,
            @NotBlank String severity,
            @NotBlank String category,
            String ownerMemberId,
            String linkedIncidentId
    ) {}

    public record UpdateRiskRequest(
            String title,
            String severity,
            String category,
            String ownerMemberId,
            @NotNull Long planRevision
    ) {}

    public record TransitionRiskRequest(
            @NotBlank String to,
            String mitigationNote,
            String resolutionNote,
            String linkedIncidentId,
            @NotNull Long planRevision
    ) {}

    public record CreateDependencyRequest(
            @NotBlank String targetRef,
            String targetProjectId,
            @NotBlank String direction,
            @NotBlank String relationship,
            @NotBlank String ownerTeam,
            String blockerReason
    ) {}

    public record UpdateDependencyRequest(
            String ownerTeam,
            String blockerReason,
            @NotNull Long planRevision
    ) {}

    public record TransitionDependencyRequest(
            @NotBlank String to,
            String rejectionReason,
            String contractCommitment,
            @NotNull Long planRevision
    ) {}

    public record CounterSignDependencyRequest(
            @NotNull Long planRevision
    ) {}

    public record DismissAiSuggestionRequest(String reason) {}

    public record InternalCreateAiSuggestionRequest(
            @NotBlank String projectId,
            @NotBlank String kind,
            @NotBlank String targetType,
            @NotBlank String targetId,
            @NotBlank String payload,
            Double confidence,
            String skillExecutionId
    ) {}
}
