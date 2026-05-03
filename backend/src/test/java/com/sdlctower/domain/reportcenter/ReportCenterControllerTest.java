package com.sdlctower.domain.reportcenter;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReportCenterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void catalogReturnsEnabledEfficiencyReports() throws Exception {
        mockMvc.perform(get(ApiConstants.REPORTS_BASE + "/catalog", "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categories[0].reports.length()").value(5))
                .andExpect(jsonPath("$.data.categories[0].reports[0].reportKey").value("eff.lead-time"));
    }

    @Test
    void runReturnsRenderedSections() throws Exception {
        mockMvc.perform(post(ApiConstants.REPORTS_BASE + "/eff.lead-time/run", "ws-default-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scope": "workspace",
                                  "scopeIds": ["ws-default-001"],
                                  "timeRange": { "preset": "last30d" },
                                  "grouping": "team",
                                  "extraFilters": {}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.headline.data.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.series.data.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.drilldown.data.rows.length()").value(greaterThan(0)));
    }

    @Test
    void runRejectsUnsupportedGrouping() throws Exception {
        mockMvc.perform(post(ApiConstants.REPORTS_BASE + "/eff.lead-time/run", "ws-default-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scope": "workspace",
                                  "scopeIds": ["ws-default-001"],
                                  "timeRange": { "preset": "last30d" },
                                  "grouping": "owner-team",
                                  "extraFilters": {}
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void runRejectsForbiddenWorkspaceScope() throws Exception {
        mockMvc.perform(post(ApiConstants.REPORTS_BASE + "/eff.lead-time/run", "ws-default-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scope": "workspace",
                                  "scopeIds": ["ws-private-001"],
                                  "timeRange": { "preset": "last30d" },
                                  "grouping": "team",
                                  "extraFilters": {}
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportReturnsAcceptedJob() throws Exception {
        mockMvc.perform(post(ApiConstants.REPORTS_BASE + "/eff.lead-time/export", "ws-default-001")
                        .queryParam("format", "csv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scope": "workspace",
                                  "scopeIds": ["ws-default-001"],
                                  "timeRange": { "preset": "last30d" },
                                  "grouping": "team",
                                  "extraFilters": {}
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("queued"));
    }

    @Test
    void historyReturnsCallerRuns() throws Exception {
        mockMvc.perform(post(ApiConstants.REPORTS_BASE + "/eff.lead-time/run", "ws-default-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "scope": "workspace",
                          "scopeIds": ["ws-default-001"],
                          "timeRange": { "preset": "last30d" },
                          "grouping": "team",
                          "extraFilters": {}
                        }
                        """)).andExpect(status().isOk());

        mockMvc.perform(get(ApiConstants.REPORTS_BASE + "/history", "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(greaterThan(0)));
    }
}
