package com.sdlctower.domain.testingmanagement;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sdlctower.shared.ApiConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@org.springframework.test.context.TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:sdlctower-tm-${random.uuid};DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TestingManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void catalogAggregateReturnsSeededData() throws Exception {
        mockMvc.perform(get(ApiConstants.TESTING + "/catalog").param("workspaceId", "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.data.totalPlans").value(4))
                .andExpect(jsonPath("$.data.grid.data.length()").value(4))
                .andExpect(jsonPath("$.data.filters.data.projectIds.length()").value(greaterThan(0)));
    }

    @Test
    void aliasBasePathAlsoWorks() throws Exception {
        mockMvc.perform(get(ApiConstants.TESTING_MANAGEMENT + "/catalog").param("workspaceId", "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.data.totalPlans").value(4));
    }

    @Test
    void planAggregateReturnsSections() throws Exception {
        mockMvc.perform(get(ApiConstants.TESTING + "/plans/plan-auth-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.header.data.name").value("Gateway Authentication Regression"))
                .andExpect(jsonPath("$.data.cases.data.length()").value(3))
                .andExpect(jsonPath("$.data.coverage.data.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.draftInbox.data.length()").value(1));
    }

    @Test
    void caseAggregateRendersAllChipColors() throws Exception {
        mockMvc.perform(get(ApiConstants.TESTING + "/cases/case-color-1102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.detail.data.linkedReqs.length()").value(4))
                .andExpect(jsonPath("$.data.detail.data.linkedReqs[0].chipColor").value("GREEN"))
                .andExpect(jsonPath("$.data.detail.data.linkedReqs[1].chipColor").value("AMBER"))
                .andExpect(jsonPath("$.data.detail.data.linkedReqs[2].chipColor").value("RED"))
                .andExpect(jsonPath("$.data.detail.data.linkedReqs[3].chipColor").value("GREY"));
    }

    @Test
    void runAggregateIncludesTruncatedFailureExcerpt() throws Exception {
        mockMvc.perform(get(ApiConstants.TESTING + "/runs/run-auth-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.header.data.planId").value("plan-auth-001"))
                .andExpect(jsonPath("$.data.caseResults.data[1].failureExcerpt").value(containsString("3ds timeout")))
                .andExpect(jsonPath("$.data.coverage.data.coveredRequirementCount").value(greaterThan(0)));
    }

    @Test
    void traceabilityAggregateReturnsBuckets() throws Exception {
        mockMvc.perform(get(ApiConstants.TESTING + "/traceability").param("workspaceId", "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.data.totalRequirements").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.reqRows.data.length()").value(greaterThan(0)));
    }
}
