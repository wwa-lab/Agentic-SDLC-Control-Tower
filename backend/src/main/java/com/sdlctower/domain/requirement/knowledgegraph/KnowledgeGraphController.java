package com.sdlctower.domain.requirement.knowledgegraph;

import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphFilters;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphImpactDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphNodeDetailDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphProviderHealthDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.GraphTriggerResultDto;
import com.sdlctower.domain.requirement.knowledgegraph.KnowledgeGraphDtos.KnowledgeGraphDto;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    public KnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
    }

    @GetMapping(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH)
    public ApiResponse<KnowledgeGraphDto> getGraph(
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) String snowGroup,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String profileId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String requirementId,
            @RequestParam(required = false) String nodeKinds,
            @RequestParam(defaultValue = "true") boolean includeIssues,
            @RequestParam(defaultValue = "false") boolean includeSuggestions,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        return ApiResponse.ok(knowledgeGraphService.getGraph(filters(workspaceId, applicationId, snowGroup, projectId, profileId, branch, requirementId, nodeKinds, includeIssues, includeSuggestions, limit)));
    }

    @GetMapping(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_NODE)
    public ApiResponse<GraphNodeDetailDto> getNode(
            @PathVariable String nodeId,
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) String snowGroup,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String profileId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String requirementId
    ) {
        return ApiResponse.ok(knowledgeGraphService.getNode(nodeId, filters(workspaceId, applicationId, snowGroup, projectId, profileId, branch, requirementId, null, true, false, 1000)));
    }

    @GetMapping(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_IMPACT)
    public ApiResponse<GraphImpactDto> getImpact(
            @RequestParam String nodeId,
            @RequestParam(defaultValue = "downstream") String direction,
            @RequestParam(defaultValue = "5") int maxDepth,
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) String snowGroup,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String profileId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String requirementId
    ) {
        return ApiResponse.ok(knowledgeGraphService.getImpact(nodeId, direction, maxDepth, filters(workspaceId, applicationId, snowGroup, projectId, profileId, branch, requirementId, null, true, false, 1000)));
    }

    @GetMapping(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_HEALTH)
    public ApiResponse<GraphProviderHealthDto> getHealth(
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) String snowGroup,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String profileId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String requirementId
    ) {
        return ApiResponse.ok(knowledgeGraphService.getHealth(filters(workspaceId, applicationId, snowGroup, projectId, profileId, branch, requirementId, null, true, false, 1000)));
    }

    @PostMapping(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_SYNC)
    public ResponseEntity<ApiResponse<GraphTriggerResultDto>> sync() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(knowledgeGraphService.queueSync()));
    }

    @PostMapping(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_IMPORT)
    public ResponseEntity<ApiResponse<GraphTriggerResultDto>> importGraph() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(knowledgeGraphService.queueImport()));
    }

    private GraphFilters filters(
            String workspaceId,
            String applicationId,
            String snowGroup,
            String projectId,
            String profileId,
            String branch,
            String requirementId,
            String nodeKinds,
            boolean includeIssues,
            boolean includeSuggestions,
            int limit
    ) {
        return new GraphFilters(workspaceId, applicationId, snowGroup, projectId, profileId, branch, requirementId, parseKinds(nodeKinds), includeIssues, includeSuggestions, limit);
    }

    private List<String> parseKinds(String nodeKinds) {
        if (nodeKinds == null || nodeKinds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(nodeKinds.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
