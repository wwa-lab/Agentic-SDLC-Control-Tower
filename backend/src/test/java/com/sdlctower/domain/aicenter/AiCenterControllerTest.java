package com.sdlctower.domain.aicenter;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
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
@org.springframework.test.context.TestPropertySource(properties =
        "spring.datasource.url=jdbc:h2:mem:sdlctower-ai-${random.uuid};DB_CLOSE_DELAY=-1")
class AiCenterControllerTest {

    private static final String WS = "WS-001";
    private static final String WS_HEADER = "X-Workspace-Id";

    @Autowired
    private MockMvc mockMvc;

    // ── Metrics ───────────────────────────────────────────────

    @Test
    void metrics_returns200_withAllFiveSections() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_METRICS).header(WS_HEADER, WS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.window").value("30d"))
                .andExpect(jsonPath("$.data.aiUsageRate.data").exists())
                .andExpect(jsonPath("$.data.adoptionRate.data").exists())
                .andExpect(jsonPath("$.data.autoExecSuccessRate.data").exists())
                .andExpect(jsonPath("$.data.timeSavedHours.data").exists())
                .andExpect(jsonPath("$.data.stageCoverageCount.data").exists())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void metrics_respectsWindowParam() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_METRICS)
                        .header(WS_HEADER, WS)
                        .param("window", "7d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.window").value("7d"));
    }

    // ── Stage Coverage ────────────────────────────────────────

    @Test
    void stageCoverage_returnsAll11Stages_inCanonicalOrder() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_STAGE_COVERAGE).header(WS_HEADER, WS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.entries", hasSize(11)))
                .andExpect(jsonPath("$.data.entries[0].stageKey").value("requirement"))
                .andExpect(jsonPath("$.data.entries[1].stageKey").value("user-story"))
                .andExpect(jsonPath("$.data.entries[10].stageKey").value("learning"))
                .andExpect(jsonPath("$.data.entries[0].covered").value(true));
    }

    // ── Skills ────────────────────────────────────────────────

    @Test
    void skills_returnsListForWorkspace() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_SKILLS).header(WS_HEADER, WS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(8)))
                .andExpect(jsonPath("$.data[0].key").exists())
                .andExpect(jsonPath("$.data[0].stages").exists());
    }

    @Test
    void skills_respectWorkspaceIsolation() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_SKILLS).header(WS_HEADER, "WS-NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    // ── Skill Detail ──────────────────────────────────────────

    @Test
    void skillDetail_returns200_forExistingKey() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER + "/skills/incident-diagnosis")
                        .header(WS_HEADER, WS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skill.key").value("incident-diagnosis"))
                .andExpect(jsonPath("$.data.skill.category").value("runtime"))
                .andExpect(jsonPath("$.data.policy").exists())
                .andExpect(jsonPath("$.data.recentRuns").exists())
                .andExpect(jsonPath("$.data.aggregateMetrics").exists());
    }

    @Test
    void skillDetail_returns404_forUnknownKey() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER + "/skills/unknown-key")
                        .header(WS_HEADER, WS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Skill not found: unknown-key"));
    }

    // ── Runs ──────────────────────────────────────────────────

    @Test
    void runs_returns200_withPage() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_RUNS).header(WS_HEADER, WS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(50))
                .andExpect(jsonPath("$.data.total").value(greaterThan(0)));
    }

    @Test
    void runs_returns400_forSize201() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_RUNS)
                        .header(WS_HEADER, WS)
                        .param("size", "201"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("size must be between 1 and 200"));
    }

    @Test
    void runs_returns400_forSize0() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_RUNS)
                        .header(WS_HEADER, WS)
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("size must be between 1 and 200"));
    }

    @Test
    void runs_filtersByStatus() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_RUNS)
                        .header(WS_HEADER, WS)
                        .param("status", "failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].status").value("failed"));
    }

    @Test
    void runs_filtersBySkillKey() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER_RUNS)
                        .header(WS_HEADER, WS)
                        .param("skillKey", "incident-diagnosis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].skillKey").value("incident-diagnosis"));
    }

    // ── Run Detail ────────────────────────────────────────────

    @Test
    void runDetail_returns200_withEvidence_and_steps() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER + "/runs/run-015")
                        .header(WS_HEADER, WS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.run.id").value("run-015"))
                .andExpect(jsonPath("$.data.run.skillKey").value("incident-diagnosis"))
                .andExpect(jsonPath("$.data.stepBreakdown", hasSize(4)))
                .andExpect(jsonPath("$.data.evidenceLinks", hasSize(3)))
                .andExpect(jsonPath("$.data.policyTrail", hasSize(2)))
                .andExpect(jsonPath("$.data.autonomyLevel").value("L2-Auto-with-approval"))
                .andExpect(jsonPath("$.data.timeSavedMinutes").value(25));
    }

    @Test
    void runDetail_returns404_forUnknownId() throws Exception {
        mockMvc.perform(get(ApiConstants.AI_CENTER + "/runs/unknown-id")
                        .header(WS_HEADER, WS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Skill execution not found: unknown-id"));
    }
}
