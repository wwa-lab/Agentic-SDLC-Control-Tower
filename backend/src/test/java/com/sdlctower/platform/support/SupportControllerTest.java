package com.sdlctower.platform.support;

import static org.assertj.core.api.Assertions.assertThat;
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

@SpringBootTest(properties = "app.support.jira-available=false")
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupportRequestRepository repository;

    @Test
    void contactUsPersistsPendingRequestWhenJiraIsUnavailable() throws Exception {
        long before = repository.count();

        mockMvc.perform(post(ApiConstants.SUPPORT_CONTACT)
                        .cookie(guestCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Need help",
                                  "category": "question",
                                  "description": "Where is the deployment dashboard?",
                                  "route": "/deployment",
                                  "reporterMode": "guest"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.requestId").exists());

        assertThat(repository.count()).isEqualTo(before + 1);
    }

    private Cookie guestCookie() throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstants.AUTH_GUEST))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie(AuthService.COOKIE_NAME);
    }
}
