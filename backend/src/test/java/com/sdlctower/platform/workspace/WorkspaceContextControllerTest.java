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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(properties = "app.auth.allow-anonymous-workspace-access=false")
class WorkspaceContextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousRequestIsRejected() throws Exception {
        mockMvc.perform(get(ApiConstants.WORKSPACE_CONTEXT, "ws-default-001"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_REQUIRED"));
    }

    @Test
    void staffRequestReturnsRealScopedContext() throws Exception {
        mockMvc.perform(get(ApiConstants.WORKSPACE_CONTEXT, "ws-default-001").cookie(loginCookie("43910516")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.workspace").value("Payment Gateway Pro"))
                .andExpect(jsonPath("$.data.application").value("app-payment-gateway-pro"))
                .andExpect(jsonPath("$.data.snowGroup").value("snow-fin-tech-ops"))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.data.snow_group").doesNotExist());
    }

    @Test
    void guestRequestReturnsDemoContext() throws Exception {
        mockMvc.perform(get(ApiConstants.WORKSPACE_CONTEXT, "demo").cookie(guestCookie()))
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
