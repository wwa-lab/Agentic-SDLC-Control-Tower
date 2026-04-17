package com.sdlctower.domain.projectspace.dto;

import com.sdlctower.domain.teamspace.dto.LinkDto;
import com.sdlctower.domain.teamspace.dto.SkillAttributionDto;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ProjectSpaceDtos {

    private ProjectSpaceDtos() {}

    public record ProjectSummaryDto(
            String id,
            String name,
            String workspaceId,
            String workspaceName,
            String applicationId,
            String applicationName,
            String lifecycleStage,
            String healthAggregate,
            List<HealthFactorDto> healthFactors,
            MemberRefDto pm,
            MemberRefDto techLead,
            MilestoneRefDto activeMilestone,
            ProjectCountersDto counters,
            Instant lastUpdatedAt,
            String teamSpaceLink
    ) {}

    public record ProjectCountersDto(
            int activeSpecs,
            int openIncidents,
            int pendingApprovals,
            int criticalHighRisks
    ) {}

    public record HealthFactorDto(String label, String severity) {}

    public record MemberRefDto(String memberId, String displayName) {}

    public record MilestoneRefDto(String id, String label, LocalDate targetDate) {}

    public record RoleAssignmentDto(
            String role,
            String memberId,
            String displayName,
            String oncallStatus,
            boolean backupPresent,
            String backupMemberId,
            String backupDisplayName
    ) {}

    public record LeadershipOwnershipDto(
            List<RoleAssignmentDto> assignments,
            LinkDto accessManagementLink
    ) {}

    public record ChainNodeHealthDto(
            String nodeKey,
            String label,
            Integer count,
            String health,
            boolean isExecutionHub,
            String deepLink,
            boolean enabled
    ) {}

    public record SdlcChainDto(List<ChainNodeHealthDto> nodes) {}

    public record MilestoneDto(
            String id,
            String label,
            LocalDate targetDate,
            String status,
            Integer percentComplete,
            MemberRefDto owner,
            boolean isCurrent,
            String slippageReason
    ) {}

    public record MilestoneHubDto(
            List<MilestoneDto> milestones,
            LinkDto projectManagementLink
    ) {}

    public record DependencyDto(
            String id,
            String targetName,
            String targetRef,
            String targetProjectId,
            boolean external,
            String direction,
            String relationship,
            String ownerTeam,
            String health,
            String blockerReason,
            LinkDto primaryAction
    ) {}

    public record DependencyMapDto(
            List<DependencyDto> upstream,
            List<DependencyDto> downstream
    ) {}

    public record RiskItemDto(
            String id,
            String title,
            String severity,
            String category,
            MemberRefDto owner,
            int ageDays,
            String latestNote,
            LinkDto primaryAction,
            SkillAttributionDto skillAttribution
    ) {}

    public record RiskRegistryDto(
            List<RiskItemDto> items,
            int total,
            Instant lastRefreshed
    ) {}

    public record VersionDriftDto(
            String band,
            int commitDelta,
            String sinceVersion,
            String description
    ) {}

    public record EnvironmentDto(
            String id,
            String label,
            String kind,
            String versionRef,
            String buildId,
            String health,
            String gateStatus,
            MemberRefDto approver,
            Instant lastDeployedAt,
            VersionDriftDto drift,
            LinkDto deploymentLink
    ) {}

    public record EnvironmentMatrixDto(List<EnvironmentDto> environments) {}

    public record ProjectSpaceAggregateDto(
            String projectId,
            String workspaceId,
            SectionResultDto<ProjectSummaryDto> summary,
            SectionResultDto<LeadershipOwnershipDto> leadership,
            SectionResultDto<SdlcChainDto> chain,
            SectionResultDto<MilestoneHubDto> milestones,
            SectionResultDto<DependencyMapDto> dependencies,
            SectionResultDto<RiskRegistryDto> risks,
            SectionResultDto<EnvironmentMatrixDto> environments
    ) {}
}
