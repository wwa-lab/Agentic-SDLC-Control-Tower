package com.sdlctower.domain.codebuildmanagement.dto;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiNoteSeverity;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiRowStatus;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.PrState;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RepoVisibility;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunTrigger;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StepConclusion;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StoryLinkStatus;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class CodeBuildManagementDtos {

    private CodeBuildManagementDtos() {}

    // ── Shared refs ──

    public record MemberRefDto(String memberId, String displayName) {}

    public record StoryChipDto(String storyId, StoryLinkStatus status, String title, String projectId) {}

    // ── Catalog ──

    public record CatalogSummaryDto(
            String workspaceId,
            int totalRepos,
            int reposWithRecentSuccess,
            int reposWithRecentFailure,
            int reposWithRunning,
            int reposWithCancelled,
            int reposWithNeutral,
            int reposWithNoRecentRuns
    ) {}

    public record CatalogRepoTileDto(
            String repoId,
            String fullName,
            String projectId,
            String projectName,
            String workspaceId,
            String defaultBranch,
            RepoVisibility visibility,
            RunStatus latestBuildStatus,
            List<RunStatus> sparkline,
            Instant lastActivityAt,
            String externalUrl
    ) {}

    public record CatalogSectionDto(
            String projectId,
            String projectName,
            List<CatalogRepoTileDto> repos
    ) {}

    public record CatalogFiltersDto(
            List<String> projectIds,
            List<RunStatus> buildStatuses,
            List<RepoVisibility> visibilities
    ) {}

    public record CatalogAggregateDto(
            SectionResultDto<CatalogSummaryDto> summary,
            SectionResultDto<List<CatalogSectionDto>> grid,
            SectionResultDto<CatalogFiltersDto> filters
    ) {}

    // ── Repo Detail ──

    public record RepoHeaderDto(
            String repoId,
            String fullName,
            String projectId,
            String projectName,
            String workspaceId,
            String workspaceName,
            String defaultBranch,
            RepoVisibility visibility,
            Instant lastSyncedAt,
            String externalUrl
    ) {}

    public record RepoBranchDto(
            String name,
            boolean isDefault,
            String headSha,
            RunStatus lastRunStatus,
            Instant lastCommitAt
    ) {}

    public record RepoRecentRunRowDto(
            String runId,
            int runNumber,
            String workflowName,
            RunStatus status,
            RunTrigger trigger,
            String branch,
            String headSha,
            String actor,
            Integer durationSec,
            Instant createdAt
    ) {}

    public record RepoRecentPrRowDto(
            String prId,
            int prNumber,
            String title,
            String author,
            String sourceBranch,
            String targetBranch,
            PrState state,
            AiNoteCountsDto aiNoteCounts,
            Instant updatedAt
    ) {}

    public record RepoRecentCommitRowDto(
            String sha,
            String shortSha,
            String author,
            String message,
            Instant committedAt,
            List<StoryChipDto> storyChips
    ) {}

    public record RepoHealthSummaryDto(
            double successRate14d,
            double medianDurationSec,
            List<FailingWorkflowDto> topFailingWorkflows
    ) {}

    public record FailingWorkflowDto(String workflowName, int failureCount) {}

    public record RepoAiSummaryDto(
            AiRowStatus status,
            String narrative,
            String skillVersion,
            Instant generatedAt,
            String error
    ) {}

    public record RepoDetailAggregateDto(
            SectionResultDto<RepoHeaderDto> header,
            SectionResultDto<List<RepoBranchDto>> branches,
            SectionResultDto<List<RepoRecentRunRowDto>> recentRuns,
            SectionResultDto<List<RepoRecentPrRowDto>> recentPrs,
            SectionResultDto<List<RepoRecentCommitRowDto>> recentCommits,
            SectionResultDto<RepoHealthSummaryDto> healthSummary,
            SectionResultDto<RepoAiSummaryDto> aiSummary
    ) {}

    // ── PR Detail ──

    public record PrHeaderDto(
            String prId,
            int prNumber,
            String repoId,
            String repoFullName,
            String title,
            String author,
            String sourceBranch,
            String targetBranch,
            PrState state,
            String headSha,
            List<StoryChipDto> storyChips,
            Instant createdAt,
            Instant updatedAt,
            String externalUrl
    ) {}

    public record PrCheckRowDto(
            String checkName,
            RunStatus status,
            StepConclusion conclusion,
            Integer durationSec,
            String runId,
            String externalUrl
    ) {}

    public record PrReviewRowDto(
            String reviewerId,
            String reviewerName,
            String state,
            String bodySummary,
            Instant submittedAt
    ) {}

    public record PrCommitRowDto(
            String sha,
            String shortSha,
            String author,
            String message,
            List<StoryChipDto> storyChips,
            Instant committedAt
    ) {}

    public record AiPrReviewNoteDto(
            String id,
            AiNoteSeverity severity,
            String filePath,
            Integer startLine,
            Integer endLine,
            String message
    ) {}

    public record AiNoteCountsDto(
            int blocker,
            int major,
            int minor,
            int info
    ) {}

    public record AiPrReviewDto(
            AiRowStatus status,
            String keyedOnSha,
            String skillVersion,
            Instant generatedAt,
            AiNoteCountsDto noteCounts,
            Map<AiNoteSeverity, List<AiPrReviewNoteDto>> notesBySeverity,
            String error
    ) {}

    public record PrDetailAggregateDto(
            SectionResultDto<PrHeaderDto> header,
            SectionResultDto<List<PrCheckRowDto>> checks,
            SectionResultDto<List<PrReviewRowDto>> reviews,
            SectionResultDto<List<PrCommitRowDto>> commits,
            SectionResultDto<AiPrReviewDto> aiReview
    ) {}

    // ── Run Detail ──

    public record RunHeaderDto(
            String runId,
            int runNumber,
            String workflowName,
            String repoId,
            String repoFullName,
            RunStatus status,
            RunTrigger trigger,
            String actor,
            String branch,
            String headSha,
            Integer durationSec,
            Instant startedAt,
            Instant completedAt,
            List<StoryChipDto> storyChips,
            String externalUrl
    ) {}

    public record RunJobRowDto(
            String jobId,
            String name,
            RunStatus status,
            StepConclusion conclusion,
            Integer durationSec,
            Instant startedAt,
            Instant completedAt,
            List<RunStepRowDto> steps
    ) {}

    public record RunStepRowDto(
            String stepId,
            String name,
            int orderIndex,
            StepConclusion conclusion,
            Instant startedAt,
            Instant completedAt,
            LogExcerptDto logExcerpt
    ) {}

    public record LogExcerptDto(
            String text,
            int byteCount,
            String externalUrl
    ) {}

    public record AiTriageRowDto(
            String rowId,
            AiRowStatus status,
            String runId,
            String jobId,
            String stepId,
            String likelyCause,
            List<String> candidateOwners,
            double confidence,
            List<String> evidence,
            String error
    ) {}

    public record AiTriageDto(
            AiRowStatus overallStatus,
            String skillVersion,
            Instant generatedAt,
            List<AiTriageRowDto> rows,
            String error
    ) {}

    public record RunRerunDto(
            String lastRerunRunId,
            RunStatus lastRerunStatus,
            Instant lastRerunAt,
            boolean rateLimited,
            Instant rateLimitResetAt
    ) {}

    public record RunDetailAggregateDto(
            SectionResultDto<RunHeaderDto> header,
            SectionResultDto<List<RunJobRowDto>> jobs,
            SectionResultDto<List<RunStepRowDto>> steps,
            SectionResultDto<String> logs,
            SectionResultDto<AiTriageDto> aiTriage,
            SectionResultDto<RunRerunDto> rerun
    ) {}

    // ── Traceability ──

    public record TraceabilityCommitRefDto(
            String sha,
            String shortSha,
            String author,
            String message,
            String repoFullName,
            StoryLinkStatus linkStatus,
            String buildRunId,
            RunStatus buildStatus,
            Instant committedAt
    ) {}

    public record TraceabilityStoryRowDto(
            String storyId,
            String title,
            String projectId,
            String projectName,
            String state,
            int linkedCommitCount,
            int linkedBuildCount,
            StoryLinkStatus worstLinkStatus,
            List<TraceabilityCommitRefDto> commits
    ) {}

    public record TraceabilitySummaryDto(
            String workspaceId,
            int knownCount,
            int unknownStoryCount,
            int noStoryIdCount,
            int ambiguousCount
    ) {}

    public record TraceabilityAggregateDto(
            SectionResultDto<TraceabilitySummaryDto> summary,
            SectionResultDto<List<TraceabilityStoryRowDto>> storyRows,
            SectionResultDto<List<TraceabilityCommitRefDto>> unknownStoryRows,
            SectionResultDto<List<TraceabilityCommitRefDto>> noStoryIdRows
    ) {}

    // ── Command request/response records ──

    public record RegenerateAiPrReviewRequest(String prevHeadSha, String reason) {}

    public record RegenerateAiTriageRequest(List<String> stepIds, String reason) {}

    public record RerunRunRequest(String reason) {}

    public record RegenerateAiPrReviewResponse(
            AiPrReviewDto aiReview,
            String skillVersion,
            Instant generatedAt
    ) {}

    public record RegenerateAiTriageResponse(
            AiTriageDto aiTriage,
            String skillVersion,
            Instant generatedAt
    ) {}

    public record RerunRunResponse(
            String newRunId,
            RunStatus status,
            Instant requestedAt
    ) {}
}
