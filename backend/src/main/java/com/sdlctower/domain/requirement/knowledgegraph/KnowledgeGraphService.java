package com.sdlctower.domain.requirement.knowledgegraph;

import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphFilters;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphNodeDetailDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphProviderHealthDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphTriggerResultDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.KnowledgeGraphDto;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeGraphService {

    private final KnowledgeGraphProperties properties;
    private final List<KnowledgeGraphProvider> providers;
    private final ProfileKnowledgeGraphProvider fallbackProvider;

    public KnowledgeGraphService(
            KnowledgeGraphProperties properties,
            List<KnowledgeGraphProvider> providers,
            ProfileKnowledgeGraphProvider fallbackProvider
    ) {
        this.properties = properties;
        this.providers = providers;
        this.fallbackProvider = fallbackProvider;
    }

    public KnowledgeGraphDto getGraph(GraphFilters filters) {
        try {
            return provider().getGraph(filters);
        } catch (RuntimeException ex) {
            return fallbackProvider.getGraph(filters);
        }
    }

    public GraphNodeDetailDto getNode(String nodeId, GraphFilters filters) {
        try {
            return provider().getNode(nodeId, filters);
        } catch (RuntimeException ex) {
            return fallbackProvider.getNode(nodeId, filters);
        }
    }

    public GraphImpactDto getImpact(String nodeId, String direction, int maxDepth, GraphFilters filters) {
        try {
            return provider().getImpact(nodeId, direction, maxDepth, filters);
        } catch (RuntimeException ex) {
            return fallbackProvider.getImpact(nodeId, direction, maxDepth, filters);
        }
    }

    public GraphProviderHealthDto getHealth(GraphFilters filters) {
        KnowledgeGraphProvider selected = provider();
        GraphProviderHealthDto health = selected.getHealth(filters);
        if (!health.available() && !selected.providerId().equals(fallbackProvider.providerId())) {
            KnowledgeGraphDto fallback = fallbackProvider.getGraph(filters);
            return new GraphProviderHealthDto(
                    selected.providerId(),
                    false,
                    true,
                    fallback.nodes().size(),
                    fallback.edges().size(),
                    fallback.issues().size(),
                    fallback.lastSync(),
                    null,
                    health.message() + " Fallback profile graph is available."
            );
        }
        return health;
    }

    public GraphTriggerResultDto queueSync() {
        return new GraphTriggerResultDto(runId("skg-sync"), "QUEUED", "Graph sync queued. Run scripts/sdd-graph-sync.mjs or the configured CI job to materialize artifacts.");
    }

    public GraphTriggerResultDto queueImport() {
        return new GraphTriggerResultDto(runId("skg-import"), "QUEUED", "Neo4j import queued. Use the local rebuild script after structured artifacts are generated.");
    }

    private KnowledgeGraphProvider provider() {
        return providers.stream()
                .filter(candidate -> candidate.supports(properties.provider()))
                .findFirst()
                .orElse(fallbackProvider);
    }

    private String runId(String prefix) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        return prefix + "-" + timestamp;
    }
}
