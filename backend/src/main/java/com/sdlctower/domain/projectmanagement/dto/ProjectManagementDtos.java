package com.sdlctower.domain.projectmanagement.dto;

import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class ProjectManagementDtos {

    private ProjectManagementDtos() {}

    public record PortfolioSummaryDto(
            String workspaceId,
            int activeProjects,
            int redProjects,
            int atRiskOrSlippedMilestones,
            int criticalRisks,
            int blockedDependencies,
            int pendingApprovals,
            int aiPendingReview,
            Instant lastRefreshedAt
    ) {}

    public record PortfolioHeatmapCellDto(String windowLabel, String dominantStatus) {}

    public record PortfolioHeatmapRowDto(
            String projectId,
            String projectName,
            List<PortfolioHeatmapCellDto> cells
    ) {}

    public record PortfolioHeatmapDto(
            String window,
            List<String> columns,
            List<PortfolioHeatmapRowDto> rows
    ) {}

    public record PortfolioCapacityProjectDto(String projectId, String projectName) {}

    public record PortfolioCapacityCellDto(String projectId, int percent) {}

    public record PortfolioCapacityRowDto(
            String memberId,
            String displayName,
            int totalPercent,
            String flag,
            List<PortfolioCapacityCellDto> cells
    ) {}

    public record PortfolioCapacityDto(
            List<PortfolioCapacityProjectDto> projects,
            List<PortfolioCapacityRowDto> rows,
            int underThreshold
    ) {}

    public record SeverityCategoryCountDto(String severity, String category, long count) {}

    public record DependencyBottleneckDto(
            String dependencyId,
            String sourceProjectId,
            String sourceProjectName,
            String targetProjectId,
            String targetDescriptor,
            boolean external,
            String relationship,
            String blockerReason,
            String ownerTeam,
            long daysBlocked,
            String aiProposalId
    ) {}

    public record CadenceMetricDto(
            String key,
            String window,
            double value,
            double deltaAbs,
            String trend
    ) {}

    public record PortfolioAggregateDto(
            SectionResultDto<PortfolioSummaryDto> summary,
            SectionResultDto<PortfolioHeatmapDto> heatmap,
            SectionResultDto<PortfolioCapacityDto> capacity,
            SectionResultDto<PortfolioRiskConcentrationDto> risks,
            SectionResultDto<List<DependencyBottleneckDto>> bottlenecks,
            SectionResultDto<List<CadenceMetricDto>> cadence
    ) {}

    public record PlanHeaderMilestoneDto(String id, String label, LocalDate targetDate) {}

    public record PlanHeaderDto(
            String projectId,
            String projectName,
            String workspaceId,
            String workspaceName,
            String applicationId,
            String applicationName,
            String lifecycleStage,
            String planHealth,
            List<String> planHealthFactors,
            PlanHeaderMilestoneDto nextMilestone,
            String pmMemberId,
            String pmDisplayName,
            String pmBackupMemberId,
            String autonomyLevel,
            Instant lastUpdatedAt,
            long planRevision
    ) {}

    public record MilestoneSlippageFactorDto(String label, String evidence) {}

    public record MilestoneSlippageDto(
            String score,
            List<MilestoneSlippageFactorDto> factors,
            Instant computedAt
    ) {}

    public record MilestoneDto(
            String id,
            String projectId,
            String label,
            String description,
            LocalDate targetDate,
            String status,
            Integer percentComplete,
            String ownerMemberId,
            String ownerDisplayName,
            String slippageReason,
            int ordering,
            MilestoneSlippageDto slippage,
            long planRevision,
            Instant createdAt,
            Instant completedAt,
            Instant archivedAt
    ) {}

    public record CapacityMilestoneRefDto(String id, String label, int ordering) {}

    public record CapacityMemberRefDto(
            String id,
            String displayName,
            boolean hasBackup,
            boolean onCall
    ) {}

    public record CapacityCellDto(
            String id,
            String memberId,
            String milestoneId,
            int percent,
            String justification,
            LocalDate windowStart,
            LocalDate windowEnd,
            long planRevision
    ) {}

    public record PlanCapacityMatrixDto(
            List<CapacityMilestoneRefDto> milestones,
            List<CapacityMemberRefDto> members,
            List<CapacityCellDto> cells,
            Map<String, Integer> rowTotals,
            Map<String, Integer> columnTotals,
            int underThreshold,
            long planRevision
    ) {}

    public record RiskDto(
            String id,
            String projectId,
            String title,
            String severity,
            String category,
            String state,
            String ownerMemberId,
            String ownerDisplayName,
            String mitigationNote,
            String resolutionNote,
            String linkedIncidentId,
            int ageDays,
            Instant detectedAt,
            Instant resolvedAt,
            long planRevision
    ) {}

    public record PortfolioRiskConcentrationDto(
            List<RiskDto> topRisks,
            List<SeverityCategoryCountDto> severityCategoryHeatmap
    ) {}

    public record DependencyDto(
            String id,
            String projectId,
            String targetRef,
            String targetProjectId,
            String targetDescriptor,
            boolean external,
            String direction,
            String relationship,
            String ownerTeam,
            String health,
            String resolutionState,
            String blockerReason,
            String contractCommitment,
            String rejectionReason,
            String counterSignatureMemberId,
            Instant counterSignedAt,
            long daysBlocked,
            long planRevision
    ) {}

    public record ProgressNodeDto(
            String node,
            int throughput,
            int priorThroughput,
            String health,
            boolean slipped,
            String deepLink
    ) {}

    public record ChangeLogEntryDto(
            String id,
            String projectId,
            String actorType,
            String actorMemberId,
            String actorDisplayName,
            String skillExecutionId,
            String action,
            String targetType,
            String targetId,
            String beforeJson,
            String afterJson,
            String reason,
            String correlationId,
            String auditLinkId,
            Instant at
    ) {}

    public record ChangeLogPageDto(
            List<ChangeLogEntryDto> entries,
            int page,
            long total
    ) {}

    public record AiSuggestionDto(
            String id,
            String projectId,
            String kind,
            String targetType,
            String targetId,
            String payload,
            double confidence,
            String state,
            String skillExecutionId,
            Instant suppressionUntil,
            Instant createdAt,
            Instant resolvedAt
    ) {}

    public record AiSuggestionActionResultDto(
            AiSuggestionDto suggestion,
            String auditLinkId
    ) {}

    public record PlanAggregateDto(
            SectionResultDto<PlanHeaderDto> header,
            SectionResultDto<List<MilestoneDto>> milestones,
            SectionResultDto<PlanCapacityMatrixDto> capacity,
            SectionResultDto<List<RiskDto>> risks,
            SectionResultDto<List<DependencyDto>> dependencies,
            SectionResultDto<List<ProgressNodeDto>> progress,
            SectionResultDto<ChangeLogPageDto> changeLog,
            SectionResultDto<List<AiSuggestionDto>> aiSuggestions
    ) {}
}
