package com.sdlctower.platform.navigation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class NavigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getNavigationEntriesReturnsExpectedOrder() throws Exception {
        mockMvc.perform(get("/api/v1/nav/entries"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          { "key": "dashboard", "label": "Dashboard", "path": "/", "comingSoon": false },
                          { "key": "team", "label": "Team Space", "path": "/team", "comingSoon": true },
                          { "key": "project-space", "label": "Project Space", "path": "/project-space", "comingSoon": false },
                          { "key": "requirements", "label": "Requirement Management", "path": "/requirements", "comingSoon": true },
                          { "key": "project-management", "label": "Project Management", "path": "/project-management", "comingSoon": true },
                          { "key": "design", "label": "Design Management", "path": "/design", "comingSoon": true },
                          { "key": "code", "label": "Code & Build", "path": "/code", "comingSoon": true },
                          { "key": "testing", "label": "Testing", "path": "/testing", "comingSoon": true },
                          { "key": "deployment", "label": "Deployment", "path": "/deployment", "comingSoon": true },
                          { "key": "incidents", "label": "Incident Management", "path": "/incidents", "comingSoon": false },
                          { "key": "ai-center", "label": "AI Center", "path": "/ai-center", "comingSoon": true },
                          { "key": "reports", "label": "Report Center", "path": "/reports", "comingSoon": true },
                          { "key": "platform", "label": "Platform Center", "path": "/platform", "comingSoon": false }
                        ]
                        """, JsonCompareMode.STRICT));
    }
}
