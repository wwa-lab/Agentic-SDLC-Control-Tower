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
                          { "key": "dashboard", "label": "Dashboard", "path": "/", "comingSoon": false, "icon": "LayoutDashboard", "order": 1 },
                          { "key": "team", "label": "Team Space", "path": "/team", "comingSoon": true, "icon": "Users", "order": 2 },
                          { "key": "project-space", "label": "Project Space", "path": "/project-space", "comingSoon": false, "icon": "Box", "order": 3 },
                          { "key": "requirements", "label": "Requirement Management", "path": "/requirements", "comingSoon": true, "icon": "FileText", "order": 4 },
                          { "key": "project-management", "label": "Project Management", "path": "/project-management", "comingSoon": true, "icon": "GitBranch", "order": 5 },
                          { "key": "design", "label": "Design Management", "path": "/design", "comingSoon": true, "icon": "Layers", "order": 6 },
                          { "key": "code", "label": "Code & Build", "path": "/code", "comingSoon": true, "icon": "Code", "order": 7 },
                          { "key": "testing", "label": "Testing", "path": "/testing", "comingSoon": true, "icon": "TestTube", "order": 8 },
                          { "key": "deployment", "label": "Deployment", "path": "/deployment", "comingSoon": true, "icon": "Send", "order": 9 },
                          { "key": "incidents", "label": "Incident Management", "path": "/incidents", "comingSoon": false, "icon": "AlertTriangle", "order": 10 },
                          { "key": "ai-center", "label": "AI Center", "path": "/ai-center", "comingSoon": true, "icon": "Cpu", "order": 11 },
                          { "key": "reports", "label": "Report Center", "path": "/reports", "comingSoon": true, "icon": "BarChart", "order": 12 },
                          { "key": "platform", "label": "Platform Center", "path": "/platform", "comingSoon": false, "icon": "Settings", "order": 13 }
                        ]
                        """, JsonCompareMode.STRICT));
    }
}
