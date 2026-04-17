package com.sdlctower.domain.designmanagement;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sdlctower.domain.projectmanagement.policy.ProjectManagementActorResolver;
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
@org.springframework.test.context.TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:sdlctower-dm-${random.uuid};DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DesignManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void catalogAggregateReturnsSeededData() throws Exception {
        mockMvc.perform(get(ApiConstants.DESIGN_MANAGEMENT + "/catalog")
                        .param("workspaceId", "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.data.totalArtifacts").value(4))
                .andExpect(jsonPath("$.data.grid.data.length()").value(4))
                .andExpect(jsonPath("$.data.filters.data.projects.length()").value(greaterThan(0)));
    }

    @Test
    void viewerAggregateReturnsSeededSections() throws Exception {
        mockMvc.perform(get(ApiConstants.DESIGN_MANAGEMENT + "/artifacts/art-2041"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.header.data.title").value("Control Tower Dashboard"))
                .andExpect(jsonPath("$.data.versions.data.length()").value(2))
                .andExpect(jsonPath("$.data.linkedSpecs.data.length()").value(2))
                .andExpect(jsonPath("$.data.aiSummary.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.changeLog.data.length()").value(greaterThan(0)));
    }

    @Test
    void traceabilityAggregateReturnsMatrixAndGaps() throws Exception {
        mockMvc.perform(get(ApiConstants.DESIGN_MANAGEMENT + "/traceability")
                        .param("workspaceId", "ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matrix.data.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.summary.data.specCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.gaps.data.length()").value(greaterThan(0)));
    }

    @Test
    void rawEndpointReturnsHtmlAndHeaders() throws Exception {
        mockMvc.perform(get(ApiConstants.DESIGN_MANAGEMENT + "/artifacts/art-2041/raw"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("cdn.tailwindcss.com")))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                .andExpect(header().string("Cache-Control", containsString("max-age=300")));
    }

    @Test
    void rawEndpointDeniesRetiredArtifactForNonAdmin() throws Exception {
        mockMvc.perform(get(ApiConstants.DESIGN_MANAGEMENT + "/artifacts/art-2044/raw")
                        .header(ProjectManagementActorResolver.ACTOR_ID_HEADER, "u-020"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(containsString("DM_ROLE_REQUIRED")));
    }

    @Test
    void publishVersionRejectsStaleVersionFence() throws Exception {
        mockMvc.perform(post(ApiConstants.DESIGN_MANAGEMENT + "/artifacts/art-2041/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prevVersionId": "art-2041-v1",
                                  "htmlPayload": "<html><body>fresh design</body></html>",
                                  "changeLogNote": "Attempt with stale fence"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("DM_STALE_VERSION")));
    }

    @Test
    void registerRejectsPiiHtml() throws Exception {
        mockMvc.perform(post(ApiConstants.DESIGN_MANAGEMENT + "/artifacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": "proj-42",
                                  "title": "PII Snapshot",
                                  "format": "HTML",
                                  "lifecycle": "DRAFT",
                                  "htmlPayload": "<html><body>owner: jane@example.com</body></html>",
                                  "changeLogNote": "Should fail"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(containsString("DM_PII_DETECTED")));
    }
}
