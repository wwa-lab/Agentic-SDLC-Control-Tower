package com.sdlctower.domain.requirement.knowledgegraph;

import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphEdgeDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphFilters;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphHealthDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactPathDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactSummaryDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphNodeDetailDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphNodeDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphProviderHealthDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphScopeDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.KnowledgeGraphDto;
import com.sdlctower.platform.profile.PipelineDocumentStageDto;
import com.sdlctower.platform.profile.PipelineProfileDto;
import com.sdlctower.platform.profile.PipelineProfileService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ProfileKnowledgeGraphProvider implements KnowledgeGraphProvider {

    private final PipelineProfileService pipelineProfileService;

    public ProfileKnowledgeGraphProvider(PipelineProfileService pipelineProfileService) {
        this.pipelineProfileService = pipelineProfileService;
    }

    @Override
    public String providerId() {
        return "profile";
    }

    @Override
    public boolean supports(String providerId) {
        return providerId == null || providerId.isBlank() || providerId().equalsIgnoreCase(providerId);
    }

    @Override
    public KnowledgeGraphDto getGraph(GraphFilters filters) {
        PipelineProfileDto profile = resolveProfile(filters);
        List<GraphNodeDto> nodes = profile.documentStages().stream()
                .filter(stage -> filters.nodeKinds().isEmpty() || filters.nodeKinds().contains("DOCUMENT"))
                .limit(filters.limit())
                .map(stage -> toNode(profile, stage, filters))
                .toList();
        Set<String> nodeIds = new HashSet<>(nodes.stream().map(GraphNodeDto::id).toList());
        List<GraphEdgeDto> edges = defaultDependencies(profile).stream()
                .filter(edge -> nodeIds.contains(edge.from()) && nodeIds.contains(edge.to()))
                .toList();
        GraphHealthDto health = new GraphHealthDto(nodes.size(), edges.size(), 0, 0, 0, 0, false, null, null);
        return new KnowledgeGraphDto(scope(filters, profile.id()), health, nodes, edges, List.of(), List.of(), null);
    }

    @Override
    public GraphNodeDetailDto getNode(String nodeId, GraphFilters filters) {
        KnowledgeGraphDto graph = getGraph(filters);
        GraphNodeDto node = graph.nodes().stream()
                .filter(candidate -> candidate.id().equals(nodeId))
                .findFirst()
                .orElse(null);
        if (node == null) {
            return new GraphNodeDetailDto(null, List.of(), List.of(), List.of());
        }
        return new GraphNodeDetailDto(
                node,
                graph.edges().stream().filter(edge -> edge.to().equals(nodeId)).toList(),
                graph.edges().stream().filter(edge -> edge.from().equals(nodeId)).toList(),
                List.of()
        );
    }

    @Override
    public GraphImpactDto getImpact(String nodeId, String direction, int maxDepth, GraphFilters filters) {
        KnowledgeGraphDto graph = getGraph(filters);
        Map<String, GraphNodeDto> nodes = new HashMap<>();
        graph.nodes().forEach(node -> nodes.put(node.id(), node));
        List<GraphImpactPathDto> paths = traverse(nodeId, normalizeDirection(direction), Math.max(1, maxDepth), graph, nodes);
        int impactedDocuments = (int) paths.stream()
                .flatMap(path -> path.nodes().stream())
                .map(GraphNodeDto::id)
                .distinct()
                .filter(id -> !id.equals(nodeId))
                .count();
        return new GraphImpactDto(
                nodeId,
                normalizeDirection(direction),
                Math.max(1, maxDepth),
                paths,
                new GraphImpactSummaryDto(impactedDocuments, 0, 0, 0)
        );
    }

    @Override
    public GraphProviderHealthDto getHealth(GraphFilters filters) {
        KnowledgeGraphDto graph = getGraph(filters);
        return new GraphProviderHealthDto(providerId(), true, false, graph.nodes().size(), graph.edges().size(), 0, null, null, "Profile fallback graph is available.");
    }

    private PipelineProfileDto resolveProfile(GraphFilters filters) {
        if (filters.profileId() != null && !filters.profileId().isBlank()) {
            return pipelineProfileService.getProfile(filters.profileId());
        }
        return pipelineProfileService.getActiveProfile(filters.workspaceId(), filters.applicationId(), filters.projectId());
    }

    private GraphScopeDto scope(GraphFilters filters, String profileId) {
        return new GraphScopeDto(
                filters.workspaceId(),
                filters.applicationId(),
                filters.snowGroup(),
                filters.projectId(),
                profileId,
                filters.branch(),
                providerId()
        );
    }

    private GraphNodeDto toNode(PipelineProfileDto profile, PipelineDocumentStageDto stage, GraphFilters filters) {
        String id = "doc-type:" + profile.id() + ":" + stage.sddType();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("docType", stage.sddType());
        props.put("profile", profile.id());
        props.put("artifactType", stage.artifactType());
        props.put("pathPattern", stage.pathPattern());
        props.put("traceabilityKey", stage.traceabilityKey());
        props.put("expectedTier", stage.expectedTier());
        props.put("requirementId", filters.requirementId());
        props.put("freshnessStatus", "UNKNOWN");
        return new GraphNodeDto(id, "DOCUMENT", stage.label(), props);
    }

    private List<GraphEdgeDto> defaultDependencies(PipelineProfileDto profile) {
        List<GraphEdgeDto> edges = new ArrayList<>();
        List<PipelineDocumentStageDto> stages = profile.documentStages();
        for (int i = 1; i < stages.size(); i += 1) {
            String from = "doc-type:" + profile.id() + ":" + stages.get(i).sddType();
            String to = "doc-type:" + profile.id() + ":" + stages.get(i - 1).sddType();
            Map<String, Object> props = Map.of("profile", profile.id(), "policy", "profile-sequence");
            edges.add(new GraphEdgeDto("edge:" + from + ":DEPENDS_ON:" + to, "DEPENDS_ON", from, to, "profile", 0.75, props));
        }
        return edges;
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
                List<String> nextNodeIds = new ArrayList<>(state.nodeIds());
                nextNodeIds.add(next);
                List<GraphEdgeDto> nextEdges = new ArrayList<>(state.edges());
                nextEdges.add(edge);
                List<GraphNodeDto> pathNodes = nextNodeIds.stream().map(nodes::get).toList();
                paths.add(new GraphImpactPathDto(nextEdges.size(), pathNodes, nextEdges));
                queue.add(new PathState(next, nextNodeIds, nextEdges));
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

    private String normalizeDirection(String direction) {
        if ("upstream".equalsIgnoreCase(direction) || "both".equalsIgnoreCase(direction)) {
            return direction.toLowerCase();
        }
        return "downstream";
    }

    private record PathState(String nodeId, List<String> nodeIds, List<GraphEdgeDto> edges) {}
}
