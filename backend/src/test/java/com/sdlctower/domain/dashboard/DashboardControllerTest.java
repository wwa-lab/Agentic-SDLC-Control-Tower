package com.sdlctower.domain.dashboard;

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
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDashboardSummaryReturnsWrappedSummary() throws Exception {
        mockMvc.perform(get(ApiConstants.DASHBOARD_SUMMARY, "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.sdlcHealth").exists())
                .andExpect(jsonPath("$.data.deliveryMetrics").exists())
                .andExpect(jsonPath("$.data.aiParticipation").exists())
                .andExpect(jsonPath("$.data.qualityMetrics").exists())
                .andExpect(jsonPath("$.data.stabilityMetrics").exists())
                .andExpect(jsonPath("$.data.governanceMetrics").exists())
                .andExpect(jsonPath("$.data.recentActivity").exists())
                .andExpect(jsonPath("$.data.valueStory").exists())
                .andExpect(jsonPath("$.data.sdlcHealth.data.length()").value(11))
                .andExpect(jsonPath("$.data.sdlcHealth.data[2].key").value("spec"))
                .andExpect(jsonPath("$.data.sdlcHealth.data[2].isHub").value(true))
                .andExpect(jsonPath("$.data.sdlcHealth.data[9].status").value("critical"))
                .andExpect(jsonPath("$.data.sdlcHealth.error").value(nullValue()))
                .andExpect(jsonPath("$.data.deliveryMetrics.error").value(nullValue()))
                .andExpect(jsonPath("$.data.aiParticipation.error").value(nullValue()))
                .andExpect(jsonPath("$.data.qualityMetrics.error").value(nullValue()))
                .andExpect(jsonPath("$.data.stabilityMetrics.error").value(nullValue()))
                .andExpect(jsonPath("$.data.governanceMetrics.error").value(nullValue()))
                .andExpect(jsonPath("$.data.recentActivity.error").value(nullValue()))
                .andExpect(jsonPath("$.data.valueStory.error").value(nullValue()))
                .andExpect(jsonPath("$.data.deliveryMetrics.data.leadTime.label").value("Lead Time"))
                .andExpect(jsonPath("$.data.recentActivity.data.entries[0].actorType").value("ai"))
                .andExpect(jsonPath("$.data.valueStory.data.headline").exists());
    }
}
