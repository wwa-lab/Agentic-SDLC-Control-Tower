package com.sdlctower.domain.incident;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sdlctower.shared.ApiConstants;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void listIncidentsReturns200WithSeverityAndIncidents() throws Exception {
        mockMvc.perform(get(ApiConstants.INCIDENTS, "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.severityDistribution.p1").value(1))
                .andExpect(jsonPath("$.data.severityDistribution.p2").value(2))
                .andExpect(jsonPath("$.data.severityDistribution.p3").value(2))
                .andExpect(jsonPath("$.data.severityDistribution.p4").value(0))
                .andExpect(jsonPath("$.data.incidents.length()").value(5))
                .andExpect(jsonPath("$.data.incidents[0].id").value("INC-0422"))
                .andExpect(jsonPath("$.data.incidents[0].priority").value("P1"))
                .andExpect(jsonPath("$.data.incidents[0].status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.incidents[4].id").value("INC-0418"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @Order(2)
    void getIncidentDetailReturns200WithAllSevenSections() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/ws-default-001/incidents/INC-0422"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.header").exists())
                .andExpect(jsonPath("$.data.header.data.id").value("INC-0422"))
                .andExpect(jsonPath("$.data.header.data.priority").value("P1"))
                .andExpect(jsonPath("$.data.header.data.autonomyLevel").value("Level2_SuggestApprove"))
                .andExpect(jsonPath("$.data.header.error").value(nullValue()))
                .andExpect(jsonPath("$.data.diagnosis").exists())
                .andExpect(jsonPath("$.data.diagnosis.data.entries.length()").value(6))
                .andExpect(jsonPath("$.data.diagnosis.data.rootCause.confidence").value("High"))
                .andExpect(jsonPath("$.data.diagnosis.data.affectedComponents.length()").value(3))
                .andExpect(jsonPath("$.data.diagnosis.error").value(nullValue()))
                .andExpect(jsonPath("$.data.skillTimeline").exists())
                .andExpect(jsonPath("$.data.skillTimeline.data.executions.length()").value(4))
                .andExpect(jsonPath("$.data.skillTimeline.error").value(nullValue()))
                .andExpect(jsonPath("$.data.actions").exists())
                .andExpect(jsonPath("$.data.actions.data.actions[0].id").value("ACT-001"))
                .andExpect(jsonPath("$.data.actions.data.actions[0].executionStatus").value("pending"))
                .andExpect(jsonPath("$.data.actions.data.actions[0].isRollbackable").value(true))
                .andExpect(jsonPath("$.data.actions.error").value(nullValue()))
                .andExpect(jsonPath("$.data.governance").exists())
                .andExpect(jsonPath("$.data.governance.data.entries.length()").value(0))
                .andExpect(jsonPath("$.data.governance.error").value(nullValue()))
                .andExpect(jsonPath("$.data.sdlcChain").exists())
                .andExpect(jsonPath("$.data.sdlcChain.data.links.length()").value(3))
                .andExpect(jsonPath("$.data.sdlcChain.error").value(nullValue()))
                .andExpect(jsonPath("$.data.learning").exists())
                .andExpect(jsonPath("$.data.learning.data").value(nullValue()))
                .andExpect(jsonPath("$.data.learning.error").value(nullValue()))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @Order(3)
    void getIncidentDetailReturns404ForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/ws-default-001/incidents/INC-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Incident not found: INC-9999"));
    }

    @Test
    @Order(4)
    void approveActionReturns200AndPersistsState() throws Exception {
        // Approve the action
        mockMvc.perform(post("/api/v1/workspaces/ws-default-001/incidents/INC-0422/actions/ACT-001/approve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actionId").value("ACT-001"))
                .andExpect(jsonPath("$.data.newStatus").value("approved"))
                .andExpect(jsonPath("$.data.governanceEntry.actor").value("leo.chen"))
                .andExpect(jsonPath("$.data.governanceEntry.actionTaken").value("approve"))
                .andExpect(jsonPath("$.error").doesNotExist());

        // Verify state persisted: re-fetch detail should show updated action + governance entry
        mockMvc.perform(get("/api/v1/workspaces/ws-default-001/incidents/INC-0422"))
                .andExpect(jsonPath("$.data.actions.data.actions[0].executionStatus").value("approved"))
                .andExpect(jsonPath("$.data.governance.data.entries.length()").value(1))
                .andExpect(jsonPath("$.data.governance.data.entries[0].actionTaken").value("approve"));
    }

    @Test
    @Order(5)
    void rejectActionReturns200WithReason() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/ws-default-001/incidents/INC-0421/actions/ACT-002/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Prefer to investigate root cause first\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actionId").value("ACT-002"))
                .andExpect(jsonPath("$.data.newStatus").value("rejected"))
                .andExpect(jsonPath("$.data.governanceEntry.actionTaken").value("reject"))
                .andExpect(jsonPath("$.data.governanceEntry.reason").value("Prefer to investigate root cause first"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @Order(6)
    void rejectActionReturns400WhenReasonMissing() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/ws-default-001/incidents/INC-0422/actions/ACT-001/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Rejection reason is required"));
    }
}
