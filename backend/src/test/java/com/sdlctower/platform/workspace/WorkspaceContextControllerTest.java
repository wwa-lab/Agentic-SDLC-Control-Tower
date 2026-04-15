package com.sdlctower.platform.workspace;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WorkspaceContextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getWorkspaceContextReturnsExpectedContract() throws Exception {
        mockMvc.perform(get("/api/v1/workspace-context"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "workspace": "Global SDLC Tower",
                          "application": "Payment-Gateway-Pro",
                          "snowGroup": "FIN-TECH-OPS",
                          "project": "Q2-Cloud-Migration",
                          "environment": "Production"
                        }
                        """, JsonCompareMode.STRICT))
                .andExpect(jsonPath("$.snowGroup").value("FIN-TECH-OPS"))
                .andExpect(jsonPath("$.snow_group").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist());
    }
}
