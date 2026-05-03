package com.sdlctower.platform.auth;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = {
        "app.auth.session-ttl-seconds=60",
        "app.auth.failed-login-limit=2",
        "app.auth.failed-login-window-seconds=60"
})
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginUsesConfiguredSessionTtlForCookie() throws Exception {
        mockMvc.perform(post(ApiConstants.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"staffId\":\"43910516\",\"password\":\"demo\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=60")));
    }

    @Test
    void repeatedFailedLoginsAreRateLimitedByStaffAndSource() throws Exception {
        String body = "{\"staffId\":\"43919998\",\"password\":\"demo\"}";

        mockMvc.perform(post(ApiConstants.AUTH_LOGIN).with(remoteAddr("10.0.0.42"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(ApiConstants.AUTH_LOGIN).with(remoteAddr("10.0.0.42"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(ApiConstants.AUTH_LOGIN).with(remoteAddr("10.0.0.42"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("LOGIN_RATE_LIMITED"));
    }

    @Test
    void disabledTeamBookStartReturnsNotFound() throws Exception {
        mockMvc.perform(get(ApiConstants.AUTH_TEAMBOOK_START))
                .andExpect(status().isNotFound());
    }

    private RequestPostProcessor remoteAddr(String remoteAddr) {
        return request -> {
            request.setRemoteAddr(remoteAddr);
            return request;
        };
    }
}
