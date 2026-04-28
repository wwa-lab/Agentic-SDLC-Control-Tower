package com.sdlctower.domain.requirement.knowledgegraph;

import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphFilters;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphNodeDetailDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphProviderHealthDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.KnowledgeGraphDto;
import org.springframework.stereotype.Component;

@Component
public class Neo4jKnowledgeGraphProvider implements KnowledgeGraphProvider {

    private final ProfileKnowledgeGraphProvider fallbackProvider;

    public Neo4jKnowledgeGraphProvider(ProfileKnowledgeGraphProvider fallbackProvider) {
        this.fallbackProvider = fallbackProvider;
    }

    @Override
    public String providerId() {
        return "neo4j";
    }

    @Override
    public boolean supports(String providerId) {
        return providerId().equalsIgnoreCase(providerId);
    }

    @Override
    public KnowledgeGraphDto getGraph(GraphFilters filters) {
        return fallbackProvider.getGraph(filters);
    }

    @Override
    public GraphNodeDetailDto getNode(String nodeId, GraphFilters filters) {
        return fallbackProvider.getNode(nodeId, filters);
    }

    @Override
    public GraphImpactDto getImpact(String nodeId, String direction, int maxDepth, GraphFilters filters) {
        return fallbackProvider.getImpact(nodeId, direction, maxDepth, filters);
    }

    @Override
    public GraphProviderHealthDto getHealth(GraphFilters filters) {
        return new GraphProviderHealthDto(providerId(), false, true, 0, 0, 0, null, null, "Neo4j provider is configured as a projection target; query fallback is serving profile data until driver-backed queries are enabled.");
    }
}
