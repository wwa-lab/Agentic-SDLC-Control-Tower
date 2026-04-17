package com.sdlctower.domain.projectspace;

import static org.hamcrest.Matchers.containsString;
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
class ProjectSpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aggregateReturns200WithAllSections() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_SPACE + "/proj-42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectId").value("proj-42"))
                .andExpect(jsonPath("$.data.workspaceId").value("ws-default-001"))
                .andExpect(jsonPath("$.data.summary.data.id").value("proj-42"))
                .andExpect(jsonPath("$.data.leadership.data.assignments.length()").value(6))
                .andExpect(jsonPath("$.data.chain.data.nodes.length()").value(11))
                .andExpect(jsonPath("$.data.milestones.data.milestones.length()").value(3))
                .andExpect(jsonPath("$.data.dependencies.data.upstream.length()").value(2))
                .andExpect(jsonPath("$.data.risks.data.total").value(2))
                .andExpect(jsonPath("$.data.environments.data.environments.length()").value(3))
                .andExpect(jsonPath("$.data.environments.error").value(nullValue()))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void summaryEndpointReturnsRawDto() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_SPACE + "/proj-42/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("proj-42"))
                .andExpect(jsonPath("$.data.applicationName").value("Payment-Gateway-Pro"))
                .andExpect(jsonPath("$.data.counters.activeSpecs").value(7))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void chainAlwaysReturns11Nodes() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_SPACE + "/proj-11/chain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes.length()").value(11))
                .andExpect(jsonPath("$.data.nodes[2].nodeKey").value("SPEC"));
    }

    @Test
    void risksAreReturnedInSeverityOrder() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_SPACE + "/proj-88/risks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$.data.items[1].severity").value("HIGH"));
    }

    @Test
    void environmentsExposeDriftBand() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_SPACE + "/proj-88/environments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.environments[2].kind").value("STAGING"))
                .andExpect(jsonPath("$.data.environments[2].drift.band").value("MAJOR"));
    }

    @Test
    void aggregateReturns400ForInvalidProjectId() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_SPACE + "/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid projectId: INVALID"));
    }

    @Test
    void aggregateReturns403ForDeniedProject() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_SPACE + "/proj-private-01"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Project access denied: proj-private-01"));
    }

    @Test
    void aggregateReturns404ForUnknownProject() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_SPACE + "/proj-missing-001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Project proj-missing-001 not found"));
    }

    @Test
    void aggregateIsolatesProjectionFailures() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_SPACE + "/proj-degraded-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.data.id").value("proj-degraded-001"))
                .andExpect(jsonPath("$.data.environments.data").value(nullValue()))
                .andExpect(jsonPath("$.data.environments.error").value(containsString("Environment")));
    }
}
