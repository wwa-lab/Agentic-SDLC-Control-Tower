package com.sdlctower.domain.requirement.knowledgegraph;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sdlctower.shared.ApiConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class KnowledgeGraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getKnowledgeGraphReturnsProfileFallback() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH).param("profileId", "standard-sdd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope.provider").value("profile"))
                .andExpect(jsonPath("$.data.scope.profileId").value("standard-sdd"))
                .andExpect(jsonPath("$.data.nodes.length()").value(9))
                .andExpect(jsonPath("$.data.edges.length()").value(8))
                .andExpect(jsonPath("$.data.health.nodeCount").value(9))
                .andExpect(jsonPath("$.data.health.edgeCount").value(8))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void getGraphNodeReturnsDirectRelationships() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_NODE, "doc-type:standard-sdd:spec")
                        .param("profileId", "standard-sdd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.node.id").value("doc-type:standard-sdd:spec"))
                .andExpect(jsonPath("$.data.incoming.length()").value(1))
                .andExpect(jsonPath("$.data.outgoing.length()").value(1));
    }

    @Test
    void getGraphImpactReturnsDownstreamPaths() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_IMPACT)
                        .param("profileId", "standard-sdd")
                        .param("nodeId", "doc-type:standard-sdd:requirement")
                        .param("direction", "downstream")
                        .param("maxDepth", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.startNodeId").value("doc-type:standard-sdd:requirement"))
                .andExpect(jsonPath("$.data.paths.length()").value(3))
                .andExpect(jsonPath("$.data.summary.impactedDocuments").value(3));
    }

    @Test
    void getGraphHealthReturnsProviderStatus() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_HEALTH).param("profileId", "ibm-i"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("profile"))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.nodeCount").value(10));
    }

    @Test
    void importAndSyncTriggersReturnAccepted() throws Exception {
        mockMvc.perform(post(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_SYNC))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("QUEUED"));
        mockMvc.perform(post(ApiConstants.REQUIREMENT_KNOWLEDGE_GRAPH_IMPORT))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("QUEUED"));
    }
}
