package com.sdlctower.domain.requirement.knowledgegraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class KnowledgeGraphDtos {

    private KnowledgeGraphDtos() {}

    public record GraphScopeDto(
            String workspaceId,
            String applicationId,
            String snowGroup,
            String projectId,
            String profileId,
            String branch,
            String provider
    ) {}

    public record GraphHealthDto(
            int nodeCount,
            int edgeCount,
            int issueCount,
            int errorCount,
            int warningCount,
            int suggestionCount,
            boolean stale,
            Instant lastGeneratedAt,
            Instant lastImportedAt
    ) {}

    public record GraphNodeDto(
            String id,
            String kind,
            String label,
            Map<String, Object> properties
    ) {}

    public record GraphEdgeDto(
            String id,
            String type,
            String from,
            String to,
            String source,
            double confidence,
            Map<String, Object> properties
    ) {}

    public record GraphIssueDto(
            String id,
            String severity,
            String code,
            String message,
            String nodeId,
            String edgeId,
            Map<String, Object> properties
    ) {}

    public record GraphSuggestionDto(
            String id,
            String type,
            String message,
            String nodeId,
            Map<String, Object> properties
    ) {}

    public record GraphRunDto(
            String runId,
            String status,
            String sourceCommitSha,
            String structuredCommitSha,
            Instant startedAt,
            Instant completedAt
    ) {}

    public record KnowledgeGraphDto(
            GraphScopeDto scope,
            GraphHealthDto health,
            List<GraphNodeDto> nodes,
            List<GraphEdgeDto> edges,
            List<GraphIssueDto> issues,
            List<GraphSuggestionDto> suggestions,
            GraphRunDto lastSync
    ) {}

    public record GraphNodeDetailDto(
            GraphNodeDto node,
            List<GraphEdgeDto> incoming,
            List<GraphEdgeDto> outgoing,
            List<GraphIssueDto> issues
    ) {}

    public record GraphImpactPathDto(
            int depth,
            List<GraphNodeDto> nodes,
            List<GraphEdgeDto> edges
    ) {}

    public record GraphImpactSummaryDto(
            int impactedDocuments,
            int impactedPrograms,
            int impactedFiles,
            int staleReviews
    ) {}

    public record GraphImpactDto(
            String startNodeId,
            String direction,
            int maxDepth,
            List<GraphImpactPathDto> paths,
            GraphImpactSummaryDto summary
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GraphProviderHealthDto(
            String provider,
            boolean available,
            boolean stale,
            int nodeCount,
            int edgeCount,
            int issueCount,
            GraphRunDto lastSync,
            ImportRunDto lastImport,
            String message
    ) {}

    public record ImportRunDto(
            String runId,
            String status,
            int nodeCount,
            int edgeCount,
            Instant completedAt
    ) {}

    public record GraphTriggerResultDto(
            String runId,
            String status,
            String message
    ) {}

    public record GraphFilters(
            String workspaceId,
            String applicationId,
            String snowGroup,
            String projectId,
            String profileId,
            String branch,
            String requirementId,
            List<String> nodeKinds,
            boolean includeIssues,
            boolean includeSuggestions,
            int limit
    ) {
        public GraphFilters {
            includeIssues = true;
            limit = limit <= 0 ? 1000 : Math.min(limit, 5000);
            nodeKinds = nodeKinds == null ? List.of() : List.copyOf(nodeKinds);
        }
    }
}
