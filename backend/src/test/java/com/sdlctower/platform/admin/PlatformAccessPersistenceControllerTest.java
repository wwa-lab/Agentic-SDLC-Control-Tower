package com.sdlctower.platform.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PlatformAccessPersistenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userLifecycleWritesAuditRows() throws Exception {
        Cookie admin = loginCookie("43910516");

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/access/users")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "staffId": "43929999",
                                  "displayName": "Persistent User",
                                  "email": "persistent@example.com",
                                  "profileSource": "manual",
                                  "status": "active"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.staffId").value("43929999"));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/access/users")
                        .cookie(admin)
                        .param("q", "43929999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].displayName").value("Persistent User"));

        mockMvc.perform(put(ApiConstants.API_V1 + "/platform/access/users/43929999")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Persistent User Updated",
                                  "email": "persistent-updated@example.com",
                                  "profileSource": "manual",
                                  "status": "inactive"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("inactive"));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/audit")
                        .cookie(admin)
                        .param("category", "permission_change")
                        .param("objectId", "43929999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].action").value("user.deactivate"));
    }

    @Test
    void roleGrantAndRevokeAreDurableAndAudited() throws Exception {
        Cookie admin = loginCookie("43910516");

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/access/users")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "staffId": "43928888",
                                  "displayName": "Scoped Viewer",
                                  "profileSource": "manual",
                                  "status": "active"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/access/assignments")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "staffId": "43928888",
                                  "role": "WORKSPACE_VIEWER",
                                  "scopeType": "workspace",
                                  "scopeId": "ws-default-001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("ra-43928888-workspace_viewer-workspace-ws-default-001"));

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/access/assignments")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "staffId": "43928888",
                                  "role": "WORKSPACE_VIEWER",
                                  "scopeType": "workspace",
                                  "scopeId": "ws-default-001"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ROLE_ASSIGNMENT_EXISTS"));

        mockMvc.perform(delete(ApiConstants.API_V1 + "/platform/access/assignments/ra-43928888-workspace_viewer-workspace-ws-default-001")
                        .cookie(admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/audit")
                        .cookie(admin)
                        .param("category", "permission_change")
                        .param("objectId", "ra-43928888-workspace_viewer-workspace-ws-default-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].action").value("role.revoke"))
                .andExpect(jsonPath("$.data.data[1].action").value("role.grant"));
    }

    @Test
    void updateUserReturnsNotFoundForUnknownStaffId() throws Exception {
        Cookie admin = loginCookie("43910516");

        mockMvc.perform(put(ApiConstants.API_V1 + "/platform/access/users/43927777")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Missing User",
                                  "email": "missing@example.com",
                                  "profileSource": "manual",
                                  "status": "active"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"));
    }

    @Test
    void lastPlatformAdminCannotBeRevokedOrDeactivated() throws Exception {
        Cookie admin = loginCookie("43910516");

        mockMvc.perform(delete(ApiConstants.API_V1 + "/platform/access/assignments/ra-backup-admin-platform")
                        .cookie(admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete(ApiConstants.API_V1 + "/platform/access/assignments/ra-admin-platform")
                        .cookie(admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("LAST_PLATFORM_ADMIN"));

        mockMvc.perform(put(ApiConstants.API_V1 + "/platform/access/users/43910516")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Platform Admin",
                                  "email": "admin@sdlctower.local",
                                  "profileSource": "manual",
                                  "status": "inactive"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("LAST_PLATFORM_ADMIN"));
    }

    private Cookie loginCookie(String staffId) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstants.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"staffId\":\"" + staffId + "\",\"password\":\"demo\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie(AuthService.COOKIE_NAME);
    }
}
