package com.sdlctower.domain.requirement.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class RequirementControlPlaneDtos {

    private RequirementControlPlaneDtos() {}

    public record SourceReferenceDto(
            String id,
            String requirementId,
            String sourceType,
            String externalId,
            String title,
            String url,
            Instant sourceUpdatedAt,
            Instant fetchedAt,
            String freshnessStatus,
            String errorMessage
    ) {}

    public record CreateSourceReferenceRequestDto(String sourceType, String url, String externalId, String title) {}

    public record SddDocumentIndexDto(
            String requirementId,
            String profileId,
            SddWorkspaceDto workspace,
            List<SddDocumentStageDto> stages
    ) {}

    public record SddWorkspaceDto(
            String id,
            String applicationId,
            String applicationName,
            String snowGroup,
            String sourceRepoFullName,
            String sddRepoFullName,
            String baseBranch,
            String workingBranch,
            String lifecycleStatus,
            String docsRoot,
            String releasePrUrl,
            String kbRepoFullName,
            String kbMainBranch,
            String kbPreviewBranch,
            String graphManifestPath
    ) {}

    public record SddDocumentStageDto(
            String id,
            String sddType,
            String stageLabel,
            String title,
            String repoFullName,
            String branchOrRef,
            String path,
            String latestCommitSha,
            String latestBlobSha,
            String githubUrl,
            String status,
            String freshnessStatus,
            boolean missing,
            DocumentQualityGateResultDto qualityGate
    ) {}

    public record SddDocumentContentDto(
            SddDocumentStageDto document,
            String markdown,
            String commitSha,
            String blobSha,
            String githubUrl,
            Instant fetchedAt
    ) {}

    public record CreateDocumentReviewRequestDto(
            String decision,
            String comment,
            String commitSha,
            String blobSha,
            String anchorType,
            String anchorValue
    ) {}

    public record DocumentReviewDto(
            String id,
            String documentId,
            String requirementId,
            String decision,
            String comment,
            String reviewerId,
            String reviewerType,
            String commitSha,
            String blobSha,
            String anchorType,
            String anchorValue,
            boolean stale,
            Instant createdAt
    ) {}

    public record CreateQualityGateRunRequestDto(String profileId, String triggerMode, String notes) {}

    public record DocumentQualityDimensionDto(String key, String label, int score, int maxScore) {}

    public record DocumentQualityFindingDto(String severity, String section, String message) {}

    public record DocumentQualityGateResultDto(
            String executionId,
            String documentId,
            String requirementId,
            String profileId,
            String sddType,
            int score,
            String band,
            boolean passed,
            int threshold,
            String rubricVersion,
            String commitSha,
            String blobSha,
            List<DocumentQualityDimensionDto> dimensions,
            List<DocumentQualityFindingDto> findings,
            String summary,
            String triggeredBy,
            String triggerMode,
            boolean stale,
            Instant scoredAt
    ) {}

    public record CreateAgentRunRequestDto(String skillKey, String targetStage, String profileId, String notes) {}

    public record AgentRunDto(
            String executionId,
            String requirementId,
            String profileId,
            String skillKey,
            String targetStage,
            String status,
            Map<String, Object> manifest,
            String command,
            String callbackUrl,
            List<AgentStageEventDto> stageEvents,
            String outputSummary,
            String errorMessage,
            List<ArtifactLinkDto> artifactLinks,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record AgentRunCallbackRequestDto(
            String status,
            Map<String, Object> outputSummary,
            List<AgentStageEventRequestDto> stageEvents,
            List<ArtifactLinkRequestDto> artifactLinks,
            String errorMessage
    ) {}

    public record AgentStageEventRequestDto(
            String stageId,
            String stageLabel,
            String state,
            String message,
            String outputPath,
            String errorMessage
    ) {}

    public record ConfirmAgentRunMergeRequestDto(String prUrl) {}

    public record AgentRunMergeConfirmationDto(
            AgentRunDto run,
            AgentStageEventDto event,
            SddDocumentIndexDto documents
    ) {}

    public record AgentStageEventDto(
            String id,
            String executionId,
            String requirementId,
            String profileId,
            String stageId,
            String stageLabel,
            String state,
            String message,
            String outputPath,
            String errorMessage,
            Instant createdAt
    ) {}

    public record ArtifactLinkRequestDto(
            String artifactType,
            String storageType,
            String title,
            String uri,
            String repoFullName,
            String path,
            String commitSha,
            String blobSha,
            String status
    ) {}

    public record ArtifactLinkDto(
            String id,
            String executionId,
            String requirementId,
            String artifactType,
            String storageType,
            String title,
            String uri,
            String repoFullName,
            String path,
            String commitSha,
            String blobSha,
            String status,
            Instant createdAt
    ) {}

    public record RequirementTraceabilityDto(
            String requirementId,
            List<SourceReferenceDto> sources,
            SddDocumentIndexDto documents,
            List<DocumentReviewDto> reviews,
            List<AgentRunDto> agentRuns,
            List<ArtifactLinkDto> artifactLinks,
            List<FreshnessItemDto> freshness
    ) {}

    public record FreshnessItemDto(
            String subjectType,
            String subjectId,
            String status,
            String message
    ) {}
}
