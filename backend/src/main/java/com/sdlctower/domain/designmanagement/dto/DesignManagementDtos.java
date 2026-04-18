package com.sdlctower.domain.designmanagement.dto;

import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class DesignManagementDtos {

    private DesignManagementDtos() {}

    public record MemberRefDto(String memberId, String displayName) {}

    public record CatalogSummaryDto(
            String workspaceId,
            long totalArtifacts,
            long linkedArtifacts,
            long draftArtifacts,
            long publishedArtifacts,
            long retiredArtifacts,
            Map<String, Long> coverageBuckets,
            Instant lastRefreshedAt,
            String advisory
    ) {}

    public record CatalogArtifactRowDto(
            String artifactId,
            String workspaceId,
            String projectId,
            String projectName,
            String title,
            String format,
            String lifecycle,
            List<MemberRefDto> authors,
            String currentVersionId,
            int currentVersionNumber,
            Instant lastUpdatedAt,
            int linkedSpecCount,
            String worstCoverageStatus,
            boolean aiSummaryReady
    ) {}

    public record CatalogSectionDto(
            String projectId,
            String projectName,
            List<CatalogArtifactRowDto> artifacts
    ) {}

    public record CatalogFiltersDto(
            List<String> projects,
            List<String> lifecycles,
            List<String> coverageStatuses
    ) {}

    public record CatalogAggregateDto(
            SectionResultDto<CatalogSummaryDto> summary,
            SectionResultDto<List<CatalogSectionDto>> grid,
            SectionResultDto<CatalogFiltersDto> filters
    ) {}

    public record ArtifactHeaderDto(
            String artifactId,
            String workspaceId,
            String projectId,
            String projectName,
            String title,
            String format,
            String lifecycle,
            List<MemberRefDto> authors,
            String currentVersionId,
            int currentVersionNumber,
            Instant registeredAt,
            MemberRefDto registeredBy,
            Instant lastUpdatedAt,
            String rawUrl
    ) {}

    public record ArtifactVersionRefDto(
            String versionId,
            int versionNumber,
            String label,
            long sizeBytes,
            String changeLogNote,
            MemberRefDto createdBy,
            Instant createdAt,
            boolean current
    ) {}

    public record LinkedSpecRowDto(
            String linkId,
            String specId,
            String requirementId,
            String requirementRoute,
            String specTitle,
            String specState,
            int coversRevision,
            int specLatestRevision,
            String declaredCoverage,
            String coverageStatus,
            MemberRefDto linkedBy,
            Instant linkedAt,
            String why
    ) {}

    public record AiSummaryPayloadDto(
            String summaryId,
            String artifactId,
            String versionId,
            String skillVersion,
            String status,
            String summaryText,
            List<String> keyElements,
            String errorMessage,
            Instant generatedAt
    ) {}

    public record ChangeLogEntryDto(
            String id,
            String entryType,
            String actorMemberId,
            String actorDisplayName,
            String reason,
            String beforeJson,
            String afterJson,
            Instant occurredAt
    ) {}

    public record ViewerAggregateDto(
            SectionResultDto<ArtifactHeaderDto> header,
            SectionResultDto<List<ArtifactVersionRefDto>> versions,
            SectionResultDto<List<LinkedSpecRowDto>> linkedSpecs,
            SectionResultDto<AiSummaryPayloadDto> aiSummary,
            SectionResultDto<List<ChangeLogEntryDto>> changeLog
    ) {}

    public record TraceabilityCellDto(
            String artifactId,
            String artifactTitle,
            String versionId,
            String coverageStatus,
            String lifecycle,
            String viewerRoute
    ) {}

    public record TraceabilityMatrixRowDto(
            String specId,
            String specTitle,
            String requirementId,
            String requirementRoute,
            String projectId,
            String projectName,
            int latestRevision,
            String specState,
            List<TraceabilityCellDto> cells,
            String overallCoverageStatus
    ) {}

    public record CoverageBucketDto(
            String status,
            long count,
            double percentage
    ) {}

    public record TraceabilitySummaryDto(
            long specCount,
            long artifactCount,
            List<CoverageBucketDto> buckets
    ) {}

    public record TraceabilityGapDto(
            String specId,
            String specTitle,
            String requirementId,
            String requirementRoute,
            String projectId,
            String projectName,
            int latestRevision,
            String specState
    ) {}

    public record TraceabilityAggregateDto(
            SectionResultDto<List<TraceabilityMatrixRowDto>> matrix,
            SectionResultDto<TraceabilitySummaryDto> summary,
            SectionResultDto<List<TraceabilityGapDto>> gaps
    ) {}

    public record MutationResultDto(
            String artifactId,
            String currentVersionId,
            Integer currentVersionNumber,
            String lifecycle,
            String message
    ) {}
}
