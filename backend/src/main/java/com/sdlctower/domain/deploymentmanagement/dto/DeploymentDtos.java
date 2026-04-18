package com.sdlctower.domain.deploymentmanagement.dto;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.*;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class DeploymentDtos {

    private DeploymentDtos() {}

    // ---- Catalog ----

    public record CatalogSummaryDto(
        long visibleApplications, long releasesLast7d, long deploysLast7d,
        double deploySuccessRate7d, double medianDeployFrequency,
        double changeFailureRate30d, Map<HealthLed, Long> byLed) {}

    public record EnvRevisionChipDto(
        String environmentName, String revisionReleaseVersion,
        DeployState deployState, Instant deployedAt, boolean isRolledBack) {}

    public record CatalogApplicationTileDto(
        String applicationId, String name, String projectId, String workspaceId,
        String runtimeLabel, Instant lastDeployAt,
        List<EnvRevisionChipDto> environmentRevisions,
        HealthLed aggregateLed, String description) {}

    public record CatalogSectionDto(String projectId, String projectName,
        List<CatalogApplicationTileDto> applications) {}

    public record CatalogFiltersDto(
        List<String> projectIds, String environmentKind,
        String deployStatus, String window, String search) {}

    public record AiWorkspaceDeploymentSummaryDto(
        AiRowStatus status, Instant generatedAt, String narrative,
        ErrorDto error) {}

    public record CatalogAggregateDto(
        SectionResultDto<CatalogSummaryDto> summary,
        SectionResultDto<List<CatalogSectionDto>> grid,
        SectionResultDto<AiWorkspaceDeploymentSummaryDto> aiSummary,
        CatalogFiltersDto filtersEcho) {}

    // ---- Application Detail ----

    public record ApplicationHeaderDto(
        String applicationId, String name, String projectId, String workspaceId,
        String runtimeLabel, String jenkinsFolderPath, String jenkinsFolderUrl,
        Instant lastDeployAt, String description) {}

    public record EnvironmentRowDto(
        String environmentName, EnvironmentKind kind,
        String currentRevision, String currentRevisionReleaseId,
        DeployState currentDeployState, Instant currentDeployedAt,
        String priorRevision, String lastGoodRevision,
        boolean isRolledBack, String rolledBackToReleaseVersion) {}

    public record RecentReleaseRowDto(
        String releaseId, String releaseVersion,
        BuildArtifactRefDto buildArtifactRef,
        Instant createdAt, ReleaseState state, long storyCount) {}

    public record RecentDeployRowDto(
        String deployId, String releaseVersion, String environmentName,
        DeployState state, Instant startedAt, Long durationSec,
        String approverDisplayName, boolean isCurrentRevision, boolean isRollback) {}

    public record TraceSummaryRowDto(
        String environmentName, long storiesLast30d, long deploysLast30d) {}

    public record ApplicationAiInsightDto(
        AiRowStatus status, Instant generatedAt, String narrative,
        List<EvidenceRefDto> evidence, String skillVersion, ErrorDto error) {}

    public record ApplicationDetailAggregateDto(
        SectionResultDto<ApplicationHeaderDto> header,
        SectionResultDto<List<EnvironmentRowDto>> environments,
        SectionResultDto<List<RecentReleaseRowDto>> recentReleases,
        SectionResultDto<List<RecentDeployRowDto>> recentDeploys,
        SectionResultDto<List<TraceSummaryRowDto>> traceSummary,
        SectionResultDto<ApplicationAiInsightDto> aiInsights) {}

    // ---- Release Detail ----

    public record ReleaseHeaderDto(
        String releaseId, String releaseVersion, String applicationId, String workspaceId,
        ReleaseState state, Instant createdAt, String createdBy,
        BuildArtifactRefDto buildArtifactRef, boolean buildArtifactResolved,
        String buildArtifactSha, String jenkinsSourceUrl) {}

    public record ReleaseCommitRowDto(
        String sha, String shortSha, String author, String message,
        Instant committedAt, List<String> storyIds) {}

    public record ReleaseDeployRowDto(
        String deployId, String environmentName, DeployState state,
        Instant startedAt, Long durationSec, String approverDisplayName,
        boolean isCurrentRevision, boolean isRollback) {}

    public record AiReleaseNotesDto(
        AiRowStatus status, String keyedOnReleaseId, String skillVersion,
        Instant generatedAt, String narrative, String diffNarrative,
        String riskHint, List<EvidenceRefDto> evidence, ErrorDto error) {}

    public record CapNoticeDto(String kind, int appliedCommitCap) {}

    public record ReleaseDetailAggregateDto(
        SectionResultDto<ReleaseHeaderDto> header,
        SectionResultDto<List<ReleaseCommitRowDto>> commits,
        SectionResultDto<List<StoryChipDto>> linkedStories,
        SectionResultDto<List<ReleaseDeployRowDto>> deploys,
        SectionResultDto<AiReleaseNotesDto> aiNotes,
        CapNoticeDto capNotice) {}

    // ---- Deploy Detail ----

    public record DeployHeaderDto(
        String deployId, String releaseId, String releaseVersion,
        String applicationId, String environmentName,
        String jenkinsJobFullName, int jenkinsBuildNumber, String jenkinsBuildUrl,
        DeployTrigger trigger, String actor,
        Instant startedAt, Instant completedAt, Long durationSec,
        DeployState state, boolean isCurrentRevision, boolean isRollback,
        boolean unresolvedFlag) {}

    public record DeployStageRowDto(
        String stageId, String name, int order, DeployStageState state,
        Instant startedAt, Instant completedAt, Long durationSec,
        String approverDisplayName, ApprovalDecision approvalDecision) {}

    public record ApprovalEventDto(
        String approvalId, String stageId, String stageName,
        String approverDisplayName, String approverMemberId, String approverRole,
        ApprovalDecision decision, String gatePolicyVersion,
        String rationale, Instant decidedAt) {}

    public record DeployArtifactRefCardDto(
        BuildArtifactRefDto buildArtifactRef, boolean buildArtifactResolved,
        BuildSummaryDto buildSummary) {}

    public record BuildSummaryDto(
        String pipelineName, int commitCount,
        String commitRangeHeadSha, String commitRangeBaseSha) {}

    public record OpenIncidentContextDto(
        String applicationId, String environmentName, String deployId,
        String releaseVersion, String deployUrl, String summaryLine) {}

    public record FollowedByRollbackDto(String deployId, String deployUrl) {}

    public record DeployDetailAggregateDto(
        SectionResultDto<DeployHeaderDto> header,
        SectionResultDto<List<DeployStageRowDto>> stageTimeline,
        SectionResultDto<List<ApprovalEventDto>> approvals,
        SectionResultDto<DeployArtifactRefCardDto> artifactRef,
        OpenIncidentContextDto openIncidentContext,
        FollowedByRollbackDto followedByRollback) {}

    // ---- Environment Detail ----

    public record EnvironmentHeaderDto(
        String applicationId, String environmentName, EnvironmentKind kind) {}

    public record EnvironmentRevisionsDto(
        String currentRevision, String currentDeployId,
        String priorRevision, String priorDeployId,
        String lastGoodRevision, String lastGoodDeployId,
        String lastFailedRevision, String lastFailedDeployId) {}

    public record EnvironmentTimelineEntryDto(
        String deployId, String releaseVersion, DeployState state,
        Instant startedAt, Long durationSec, boolean isRollback) {}

    public record EnvironmentMetricsDto(
        double changeFailureRate30d, Long mttrSec30d,
        long deployCount30d, long rollbackCount30d, double deploymentFrequency30d) {}

    public record EnvironmentDetailAggregateDto(
        SectionResultDto<EnvironmentHeaderDto> header,
        SectionResultDto<EnvironmentRevisionsDto> revisions,
        SectionResultDto<List<EnvironmentTimelineEntryDto>> timeline,
        SectionResultDto<EnvironmentMetricsDto> metrics) {}

    // ---- Traceability ----

    public record StoryChipDto(
        String storyId, String status, String title, String projectId) {}

    public record TraceabilityReleaseRowDto(
        String releaseId, String releaseVersion,
        String applicationId, String applicationName,
        Instant createdAt, ReleaseState state) {}

    public record TraceabilityDeployEntryDto(
        String deployId, String releaseVersion, DeployState state,
        Instant startedAt, boolean isCurrentRevision, boolean isRollback) {}

    public record TraceabilityDeployGroupDto(
        String environmentName, EnvironmentKind kind,
        List<TraceabilityDeployEntryDto> deploys) {}

    public record TraceabilityAggregateDto(
        StoryChipDto storyChip,
        SectionResultDto<List<TraceabilityReleaseRowDto>> releases,
        SectionResultDto<List<TraceabilityDeployGroupDto>> deploysByEnvironment,
        boolean upstreamAvailable) {}

    // ---- Shared sub-DTOs ----

    public record BuildArtifactRefDto(String sliceId, String buildArtifactId) {}

    public record EvidenceRefDto(String kind, String id, String label) {}

    public record ErrorDto(String code, String message) {}

    // ---- Commands ----

    public record RegenerateReleaseNotesResponse(
        String releaseId, AiRowStatus status, Instant generatedAt) {}

    public record RegenerateDeploySummaryResponse(
        String deployId, AiRowStatus status, Instant generatedAt) {}

    public record RegenerateWorkspaceSummaryResponse(
        String workspaceId, AiRowStatus status, Instant generatedAt) {}

    // ---- Cross-slice facade ----

    public record DeploymentHealthDto(
        double deploymentFrequencyPerDay, double changeFailureRate,
        long mttrMinutes, DeployState lastDeployState, Instant lastDeployAt) {}
}
