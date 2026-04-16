package com.sdlctower.platform.workspace;

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
class WorkspaceContextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getWorkspaceContextReturnsWrappedDto() throws Exception {
        mockMvc.perform(get(ApiConstants.WORKSPACE_CONTEXT))
                .andExpect(status().isOk())
                // Verify ApiResponse envelope
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data").exists())
                // Verify DTO fields inside data
                .andExpect(jsonPath("$.data.workspace").value("Global SDLC Tower"))
                .andExpect(jsonPath("$.data.application").value("Payment-Gateway-Pro"))
                .andExpect(jsonPath("$.data.snowGroup").value("FIN-TECH-OPS"))
                .andExpect(jsonPath("$.data.project").value("Q2-Cloud-Migration"))
                .andExpect(jsonPath("$.data.environment").value("Production"))
                // Verify entity internals are NOT leaked
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.data.snow_group").doesNotExist());
    }
}
