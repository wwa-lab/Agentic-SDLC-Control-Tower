package com.sdlctower.domain.projectmanagement;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sdlctower.shared.ApiConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@org.springframework.test.context.TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:sdlctower-pm-${random.uuid};DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProjectManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void portfolioAggregateReturnsSeededSections() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_MANAGEMENT + "/portfolio")
                        .param("workspaceId", "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.data.workspaceId").value("ws-default-001"))
                .andExpect(jsonPath("$.data.summary.data.activeProjects").value(6))
                .andExpect(jsonPath("$.data.heatmap.data.rows.length()").value(6))
                .andExpect(jsonPath("$.data.capacity.data.rows.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.risks.data.topRisks[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$.data.bottlenecks.data.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.cadence.data.length()").value(6));
    }

    @Test
    void planAggregateReturnsAllSections() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_MANAGEMENT + "/plan/proj-42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.header.data.projectId").value("proj-42"))
                .andExpect(jsonPath("$.data.milestones.data.length()").value(3))
                .andExpect(jsonPath("$.data.capacity.data.cells.length()").value(3))
                .andExpect(jsonPath("$.data.risks.data.length()").value(2))
                .andExpect(jsonPath("$.data.dependencies.data.length()").value(4))
                .andExpect(jsonPath("$.data.progress.data.length()").value(11))
                .andExpect(jsonPath("$.data.changeLog.data.entries.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.aiSuggestions.data.length()").value(1));
    }

    @Test
    void milestoneTransitionRejectsStaleRevision() throws Exception {
        mockMvc.perform(post(ApiConstants.PROJECT_MANAGEMENT + "/plan/proj-42/milestones/MS-PROJ42-03/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "SLIPPED",
                                  "slippageReason": "Enough context for a stale revision check",
                                  "planRevision": 3
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("PM_STALE_REVISION")));
    }

    @Test
    void capacityBatchUpdateReturnsUpdatedRevision() throws Exception {
        mockMvc.perform(patch(ApiConstants.PROJECT_MANAGEMENT + "/plan/proj-42/capacity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "edits": [
                                    {
                                      "memberId": "u-011",
                                      "milestoneId": "MS-PROJ42-02",
                                      "percent": 65,
                                      "justification": null,
                                      "planRevision": 4
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planRevision").value(5))
                .andExpect(jsonPath(
                        "$.data.cells[?(@.memberId=='u-011' && @.milestoneId=='MS-PROJ42-02')].percent"
                ).value(hasItem(65)));
    }

    @Test
    void aiDismissSetsSuppressionWindow() throws Exception {
        mockMvc.perform(post(ApiConstants.PROJECT_MANAGEMENT + "/plan/proj-55/ai-suggestions/sug-p55-r1/dismiss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Handled in standup already"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.suggestion.state").value("DISMISSED"))
                .andExpect(jsonPath("$.data.suggestion.suppressionUntil").exists());
    }

    @Test
    void counterSignRequiresTargetAuthority() throws Exception {
        mockMvc.perform(post(ApiConstants.PROJECT_MANAGEMENT + "/plan/proj-42/dependencies/DEP-P42-UP-2/countersign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-PM-ACTOR-ID", "u-020")
                        .content("""
                                {
                                  "planRevision": 4
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(containsString("PM_AUTH_FORBIDDEN")));
    }

    @Test
    void privateWorkspacePortfolioIsDenied() throws Exception {
        mockMvc.perform(get(ApiConstants.PROJECT_MANAGEMENT + "/portfolio")
                        .param("workspaceId", "ws-private-001"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(containsString("PM_AUTH_FORBIDDEN")));
    }
}
