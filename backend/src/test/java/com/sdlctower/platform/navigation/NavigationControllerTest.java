package com.sdlctower.platform.navigation;

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
class NavigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getNavigationEntriesReturnsWrappedList() throws Exception {
        mockMvc.perform(get(ApiConstants.NAV_ENTRIES))
                .andExpect(status().isOk())
                // Verify ApiResponse envelope
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(13))
                // Verify first and last entries
                .andExpect(jsonPath("$.data[0].key").value("dashboard"))
                .andExpect(jsonPath("$.data[0].label").value("Dashboard"))
                .andExpect(jsonPath("$.data[0].icon").value("LayoutDashboard"))
                .andExpect(jsonPath("$.data[0].order").value(1))
                .andExpect(jsonPath("$.data[12].key").value("platform"))
                .andExpect(jsonPath("$.data[12].label").value("Platform Center"))
                .andExpect(jsonPath("$.data[12].order").value(13));
    }
}
