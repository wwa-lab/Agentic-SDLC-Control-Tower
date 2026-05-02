package com.sdlctower.platform.admin;

import static org.hamcrest.Matchers.hasItem;
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
class PlatformCenterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void platformReadsRequirePlatformAdmin() throws Exception {
        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/templates"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_REQUIRED"));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/templates").cookie(guestCookie()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("PLATFORM_ADMIN_REQUIRED"));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/templates").cookie(loginCookie("43910000")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("PLATFORM_ADMIN_REQUIRED"));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/templates").cookie(loginCookie("43910516")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].id").value("tpl-page-control"));
    }

    @Test
    void accessMeRequiresPlatformAdmin() throws Exception {
        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/access/me").cookie(guestCookie()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/access/me").cookie(loginCookie("43910516")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles").value(hasItem("PLATFORM_ADMIN")));
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
