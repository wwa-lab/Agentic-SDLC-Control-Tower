package com.sdlctower.platform.workspace;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sdlctower.platform.auth.AuthService;
import com.sdlctower.shared.ApiConstants;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WorkspaceContextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousRequestIsRejected() throws Exception {
        mockMvc.perform(get(ApiConstants.WORKSPACE_CONTEXT))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_REQUIRED"));
    }

    @Test
    void staffRequestReturnsRealScopedContext() throws Exception {
        mockMvc.perform(get(ApiConstants.WORKSPACE_CONTEXT).cookie(loginCookie("43910516")))
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

    @Test
    void guestRequestReturnsDemoContext() throws Exception {
        mockMvc.perform(get(ApiConstants.WORKSPACE_CONTEXT).cookie(guestCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workspaceId").value("demo-workspace"))
                .andExpect(jsonPath("$.data.applicationId").value("demo-application"))
                .andExpect(jsonPath("$.data.snowGroupId").value("demo-snow-group"))
                .andExpect(jsonPath("$.data.demoMode").value(true));
    }

    private Cookie loginCookie(String staffId) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstants.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"staffId\":\"" + staffId + "\",\"password\":\"demo\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie(AuthService.COOKIE_NAME);
    }

    private Cookie guestCookie() throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstants.AUTH_GUEST))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie(AuthService.COOKIE_NAME);
    }
}
