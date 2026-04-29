package com.sdlctower.domain.requirement.knowledgegraph;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphEdgeDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphFilters;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphHealthDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactPathDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactSummaryDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphIssueDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphNodeDetailDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphNodeDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphProviderHealthDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphRunDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphScopeDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphSuggestionDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.ImportRunDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.KnowledgeGraphDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ManifestKnowledgeGraphProvider implements KnowledgeGraphProvider {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final KnowledgeGraphProperties properties;
    private final ObjectMapper objectMapper;

    public ManifestKnowledgeGraphProvider(KnowledgeGraphProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerId() {
        return "manifest";
    }

    @Override
    public boolean supports(String providerId) {
        return providerId().equalsIgnoreCase(providerId);
    }

    @Override
    public KnowledgeGraphDto getGraph(GraphFilters filters) {
        GraphArtifactSet artifacts = readArtifacts(filters);
        List<GraphNodeDto> nodes = artifacts.nodes().stream()
                .filter(node -> filters.nodeKinds().isEmpty() || filters.nodeKinds().contains(node.kind()))
                .filter(node -> matches(filters, node.properties()))
                .limit(filters.limit())
                .toList();
        Set<String> nodeIds = Set.copyOf(nodes.stream().map(GraphNodeDto::id).toList());
        List<GraphEdgeDto> edges = artifacts.edges().stream()
                .filter(edge -> nodeIds.contains(edge.from()) && nodeIds.contains(edge.to()))
                .toList();
        List<GraphIssueDto> issues = filters.includeIssues()
                ? artifacts.issues().stream().filter(issue -> issue.nodeId() == null || nodeIds.contains(issue.nodeId())).toList()
                : List.of();
        List<GraphSuggestionDto> suggestions = filters.includeSuggestions()
                ? artifacts.suggestions().stream().filter(suggestion -> suggestion.nodeId() == null || nodeIds.contains(suggestion.nodeId())).toList()
                : List.of();
        GraphHealthDto health = new GraphHealthDto(
                nodes.size(),
                edges.size(),
                issues.size(),
                (int) issues.stream().filter(issue -> "ERROR".equalsIgnoreCase(issue.severity())).count(),
                (int) issues.stream().filter(issue -> "WARNING".equalsIgnoreCase(issue.severity())).count(),
                suggestions.size(),
                stale(artifacts.manifest()),
                instantValue(artifacts.manifest(), "generatedAt"),
                instantValue(artifacts.manifest(), "importedAt")
        );
        return new KnowledgeGraphDto(scope(filters, artifacts.manifest()), health, nodes, edges, issues, suggestions, lastSync(artifacts.manifest()));
    }

    @Override
    public GraphNodeDetailDto getNode(String nodeId, GraphFilters filters) {
        KnowledgeGraphDto graph = getGraph(filters);
        GraphNodeDto node = graph.nodes().stream().filter(candidate -> candidate.id().equals(nodeId)).findFirst().orElse(null);
        if (node == null) {
            return new GraphNodeDetailDto(null, List.of(), List.of(), List.of());
        }
        return new GraphNodeDetailDto(
                node,
                graph.edges().stream().filter(edge -> edge.to().equals(nodeId)).toList(),
                graph.edges().stream().filter(edge -> edge.from().equals(nodeId)).toList(),
                graph.issues().stream().filter(issue -> nodeId.equals(issue.nodeId())).toList()
        );
    }

    @Override
    public GraphImpactDto getImpact(String nodeId, String direction, int maxDepth, GraphFilters filters) {
        KnowledgeGraphDto graph = getGraph(filters);
        Map<String, GraphNodeDto> nodes = new HashMap<>();
        graph.nodes().forEach(node -> nodes.put(node.id(), node));
        String normalizedDirection = normalizeDirection(direction);
        List<GraphImpactPathDto> paths = traverse(nodeId, normalizedDirection, Math.max(1, maxDepth), graph, nodes);
        Set<String> impactedNodeIds = Set.copyOf(paths.stream()
                .flatMap(path -> path.nodes().stream())
                .map(GraphNodeDto::id)
                .filter(id -> !id.equals(nodeId))
                .toList());
        int impactedPrograms = countByKind(nodes, impactedNodeIds, "PROGRAM");
        int impactedFiles = countByKind(nodes, impactedNodeIds, "FILE");
        int staleReviews = (int) impactedNodeIds.stream()
                .map(nodes::get)
                .filter(node -> node != null && "DOCUMENT_CHANGED_AFTER_REVIEW".equals(node.properties().get("freshnessStatus")))
                .count();
        return new GraphImpactDto(
                nodeId,
                normalizedDirection,
                Math.max(1, maxDepth),
                paths,
                new GraphImpactSummaryDto(impactedNodeIds.size(), impactedPrograms, impactedFiles, staleReviews)
        );
    }

    @Override
    public GraphProviderHealthDto getHealth(GraphFilters filters) {
        try {
            KnowledgeGraphDto graph = getGraph(filters);
            return new GraphProviderHealthDto(providerId(), true, graph.health().stale(), graph.nodes().size(), graph.edges().size(), graph.issues().size(), graph.lastSync(), lastImport(graph), "Manifest graph artifacts are available.");
        } catch (IllegalStateException ex) {
            return new GraphProviderHealthDto(providerId(), false, true, 0, 0, 0, null, null, ex.getMessage());
        }
    }

    private GraphArtifactSet readArtifacts(GraphFilters filters) {
        Path graphDir = graphDirectory(filters);
        if (!Files.exists(graphDir.resolve("manifest.json"))) {
            throw new IllegalStateException("Graph manifest not found at " + graphDir.resolve("manifest.json"));
        }
        try {
            Map<String, Object> manifest = objectMapper.readValue(graphDir.resolve("manifest.json").toFile(), MAP_TYPE);
            return new GraphArtifactSet(
                    manifest,
                    readJsonl(graphDir.resolve("nodes.jsonl")).stream().map(this::toNode).toList(),
                    readJsonl(graphDir.resolve("edges.jsonl")).stream().map(this::toEdge).toList(),
                    readOptionalJsonl(graphDir.resolve("issues.jsonl")).stream().map(this::toIssue).toList(),
                    readOptionalJsonl(graphDir.resolve("suggestions.jsonl")).stream().map(this::toSuggestion).toList()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read graph artifacts from " + graphDir, ex);
        }
    }

    private Path graphDirectory(GraphFilters filters) {
        Path root = properties.manifestRoot().isBlank() ? Path.of(".") : Path.of(properties.manifestRoot());
        Path direct = root.resolve("_graph");
        if (Files.exists(direct.resolve("manifest.json"))) {
            return direct;
        }
        if (filters.branch() != null && !filters.branch().isBlank()) {
            Path branchGraph = root.resolve(sanitize(filters.branch())).resolve("_graph");
            if (Files.exists(branchGraph.resolve("manifest.json"))) {
                return branchGraph;
            }
        }
        return direct;
    }

    private List<Map<String, Object>> readJsonl(Path path) throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String line : Files.readAllLines(path)) {
            if (!line.isBlank()) {
                rows.add(objectMapper.readValue(line, MAP_TYPE));
            }
        }
        return rows;
    }

    private List<Map<String, Object>> readOptionalJsonl(Path path) throws IOException {
        return Files.exists(path) ? readJsonl(path) : List.of();
    }

    @SuppressWarnings("unchecked")
    private GraphNodeDto toNode(Map<String, Object> row) {
        return new GraphNodeDto(
                stringValue(row, "id"),
                stringValue(row, "kind", "DOCUMENT"),
                stringValue(row, "label", stringValue(row, "id")),
                mapValue(row, "properties")
        );
    }

    private GraphEdgeDto toEdge(Map<String, Object> row) {
        return new GraphEdgeDto(
                stringValue(row, "id"),
                stringValue(row, "type", "DEPENDS_ON"),
                stringValue(row, "from"),
                stringValue(row, "to"),
                stringValue(row, "source", "manifest"),
                doubleValue(row, "confidence", 1.0),
                mapValue(row, "properties")
        );
    }

    private GraphIssueDto toIssue(Map<String, Object> row) {
        return new GraphIssueDto(
                stringValue(row, "id"),
                stringValue(row, "severity", "WARNING"),
                stringValue(row, "code", "GRAPH_ISSUE"),
                stringValue(row, "message"),
                stringValue(row, "nodeId", null),
                stringValue(row, "edgeId", null),
                mapValue(row, "properties")
        );
    }

    private GraphSuggestionDto toSuggestion(Map<String, Object> row) {
        return new GraphSuggestionDto(
                stringValue(row, "id"),
                stringValue(row, "type", "SUGGESTION"),
                stringValue(row, "message"),
                stringValue(row, "nodeId", null),
                mapValue(row, "properties")
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        return new LinkedHashMap<>();
    }

    private boolean matches(GraphFilters filters, Map<String, Object> properties) {
        return matchesValue(filters.workspaceId(), properties.get("workspaceId"))
                && matchesValue(filters.applicationId(), properties.get("applicationId"))
                && matchesValue(filters.snowGroup(), properties.get("snowGroup"))
                && matchesValue(filters.projectId(), properties.get("projectId"))
                && matchesValue(filters.profileId(), properties.get("profile"))
                && matchesValue(filters.branch(), properties.get("branch"))
                && matchesValue(filters.requirementId(), properties.get("requirementId"));
    }

    private boolean matchesValue(String filter, Object value) {
        return filter == null || filter.isBlank() || filter.equals(String.valueOf(value));
    }

    private GraphScopeDto scope(GraphFilters filters, Map<String, Object> manifest) {
        return new GraphScopeDto(
                firstNonBlank(filters.workspaceId(), stringValue(manifest, "workspaceId", null)),
                firstNonBlank(filters.applicationId(), stringValue(manifest, "applicationId", null)),
                firstNonBlank(filters.snowGroup(), stringValue(manifest, "snowGroup", null)),
                firstNonBlank(filters.projectId(), stringValue(manifest, "projectId", null)),
                firstNonBlank(filters.profileId(), stringValue(manifest, "profileId", null)),
                firstNonBlank(filters.branch(), stringValue(manifest, "branch", null)),
                providerId()
        );
    }

    private GraphRunDto lastSync(Map<String, Object> manifest) {
        Object raw = manifest.get("lastSync");
        if (raw instanceof Map<?, ?> sync) {
            return new GraphRunDto(
                    String.valueOf(sync.get("runId")),
                    String.valueOf(sync.get("status")),
                    sync.get("sourceCommitSha") == null ? null : String.valueOf(sync.get("sourceCommitSha")),
                    sync.get("structuredCommitSha") == null ? null : String.valueOf(sync.get("structuredCommitSha")),
                    parseInstant(sync.get("startedAt")),
                    parseInstant(sync.get("completedAt"))
            );
        }
        return null;
    }

    private ImportRunDto lastImport(KnowledgeGraphDto graph) {
        if (graph.health().lastImportedAt() == null) {
            return null;
        }
        return new ImportRunDto("manifest-import", "COMPLETED", graph.nodes().size(), graph.edges().size(), graph.health().lastImportedAt());
    }

    private boolean stale(Map<String, Object> manifest) {
        Object stale = manifest.get("stale");
        return stale instanceof Boolean value && value;
    }

    private Instant instantValue(Map<String, Object> row, String key) {
        return parseInstant(row.get(key));
    }

    private Instant parseInstant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String stringValue(Map<String, Object> row, String key) {
        return stringValue(row, key, "");
    }

    private String stringValue(Map<String, Object> row, String key, String fallback) {
        Object value = row.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private double doubleValue(Map<String, Object> row, String key, double fallback) {
        Object value = row.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private String sanitize(String branch) {
        return branch.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "-");
    }

    private String normalizeDirection(String direction) {
        if ("upstream".equalsIgnoreCase(direction) || "both".equalsIgnoreCase(direction)) {
            return direction.toLowerCase(Locale.ROOT);
        }
        return "downstream";
    }

    private List<GraphImpactPathDto> traverse(String startNodeId, String direction, int maxDepth, KnowledgeGraphDto graph, Map<String, GraphNodeDto> nodes) {
        List<GraphImpactPathDto> paths = new ArrayList<>();
        Queue<PathState> queue = new ArrayDeque<>();
        queue.add(new PathState(startNodeId, List.of(startNodeId), List.of()));
        while (!queue.isEmpty()) {
            PathState state = queue.remove();
            if (state.edges().size() >= maxDepth) {
                continue;
            }
            for (GraphEdgeDto edge : adjacentEdges(state.nodeId(), direction, graph.edges())) {
                String next = edge.from().equals(state.nodeId()) ? edge.to() : edge.from();
                if (state.nodeIds().contains(next) || !nodes.containsKey(next)) {
                    continue;
                }
                List<String> nodeIds = new ArrayList<>(state.nodeIds());
                nodeIds.add(next);
                List<GraphEdgeDto> edges = new ArrayList<>(state.edges());
                edges.add(edge);
                paths.add(new GraphImpactPathDto(edges.size(), nodeIds.stream().map(nodes::get).toList(), edges));
                queue.add(new PathState(next, nodeIds, edges));
            }
        }
        return paths;
    }

    private List<GraphEdgeDto> adjacentEdges(String nodeId, String direction, List<GraphEdgeDto> edges) {
        return edges.stream()
                .filter(edge -> ("upstream".equals(direction) || "both".equals(direction)) && edge.from().equals(nodeId)
                        || ("downstream".equals(direction) || "both".equals(direction)) && edge.to().equals(nodeId))
                .toList();
    }

    private int countByKind(Map<String, GraphNodeDto> nodes, Set<String> ids, String kind) {
        return (int) ids.stream()
                .map(nodes::get)
                .filter(node -> node != null && kind.equals(node.kind()))
                .count();
    }

    private record GraphArtifactSet(
            Map<String, Object> manifest,
            List<GraphNodeDto> nodes,
            List<GraphEdgeDto> edges,
            List<GraphIssueDto> issues,
            List<GraphSuggestionDto> suggestions
    ) {}

    private record PathState(String nodeId, List<String> nodeIds, List<GraphEdgeDto> edges) {}
}
