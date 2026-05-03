package com.sdlctower.domain.teamspace;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sdlctower.shared.ApiConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class TeamSpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aggregateReturns200WithAllSections() throws Exception {
        mockMvc.perform(get(ApiConstants.TEAM_SPACE, "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workspaceId").value("ws-default-001"))
                .andExpect(jsonPath("$.data.summary.data.id").value("ws-default-001"))
                .andExpect(jsonPath("$.data.summary.data.name").value("Global SDLC Tower"))
                .andExpect(jsonPath("$.data.operatingModel.data.approvalMode.value").value("REVIEWER_REQUIRED"))
                .andExpect(jsonPath("$.data.members.data.members.length()").value(3))
                .andExpect(jsonPath("$.data.templates.data.groups.PAGE.length()").value(1))
                .andExpect(jsonPath("$.data.pipeline.data.chain.length()").value(11))
                .andExpect(jsonPath("$.data.metrics.data.deliveryEfficiency.length()").value(2))
                .andExpect(jsonPath("$.data.risks.data.total").value(3))
                .andExpect(jsonPath("$.data.projects.data.groups.AT_RISK.length()").value(2))
                .andExpect(jsonPath("$.data.metrics.error").value(nullValue()))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void summaryEndpointReturnsRawDto() throws Exception {
        mockMvc.perform(get(ApiConstants.TEAM_SPACE + "/summary", "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("ws-default-001"))
                .andExpect(jsonPath("$.data.responsibilityBoundary.projectCount").value(7))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void unknownWorkspaceReturns404() throws Exception {
        mockMvc.perform(get(ApiConstants.TEAM_SPACE, "ws-missing-001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("WORKSPACE_NOT_FOUND"));
    }
}
