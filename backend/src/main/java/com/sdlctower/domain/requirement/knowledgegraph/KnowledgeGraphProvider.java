package com.sdlctower.domain.requirement.knowledgegraph;

import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphFilters;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphNodeDetailDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphProviderHealthDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.KnowledgeGraphDto;

public interface KnowledgeGraphProvider {

    String providerId();

    boolean supports(String providerId);

    KnowledgeGraphDto getGraph(GraphFilters filters);

    GraphNodeDetailDto getNode(String nodeId, GraphFilters filters);

    GraphImpactDto getImpact(String nodeId, String direction, int maxDepth, GraphFilters filters);

    GraphProviderHealthDto getHealth(GraphFilters filters);
}
