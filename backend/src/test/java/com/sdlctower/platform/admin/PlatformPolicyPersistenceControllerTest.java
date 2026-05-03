package com.sdlctower.platform.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
class PlatformPolicyPersistenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void policiesAndSeedExceptionsReadFromRepository() throws Exception {
        Cookie admin = loginCookie("43910516");

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/policies")
                        .cookie(admin)
                        .param("category", "approval"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].id").value("pol-release-approval"))
                .andExpect(jsonPath("$.data.data[0].body.required").value(true));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/policies/pol-release-approval")
                        .cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.key").value("release-approval"))
                .andExpect(jsonPath("$.data.version").value(1));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/policies/pol-release-approval/exceptions")
                        .cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("pex-release-approval-demo"))
                .andExpect(jsonPath("$.data[0].revokedAt").isEmpty());
    }

    @Test
    void policyLifecycleCreatesVersionsAndAuditRecords() throws Exception {
        Cookie admin = loginCookie("43910516");

        MvcResult created = mockMvc.perform(post(ApiConstants.API_V1 + "/platform/policies")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "workspace-autonomy",
                                  "name": "Workspace Autonomy",
                                  "category": "autonomy",
                                  "scopeType": "workspace",
                                  "scopeId": "ws-default-001",
                                  "boundTo": "ai.autonomy",
                                  "status": "active",
                                  "body": { "level": "supervised" }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.key").value("workspace-autonomy"))
                .andExpect(jsonPath("$.data.version").value(1))
                .andReturn();

        String policyId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/policies")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "workspace-autonomy",
                                  "name": "Workspace Autonomy Duplicate",
                                  "category": "autonomy",
                                  "scopeType": "workspace",
                                  "scopeId": "ws-default-001",
                                  "status": "active",
                                  "body": { "level": "autonomous" }
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("POLICY_ALREADY_EXISTS"));

        MvcResult updated = mockMvc.perform(put(ApiConstants.API_V1 + "/platform/policies/" + policyId)
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "workspace-autonomy",
                                  "name": "Workspace Autonomy",
                                  "category": "autonomy",
                                  "scopeType": "workspace",
                                  "scopeId": "ws-default-001",
                                  "boundTo": "ai.autonomy",
                                  "status": "inactive",
                                  "body": { "level": "collaborative" }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(2))
                .andExpect(jsonPath("$.data.status").value("active"))
                .andExpect(jsonPath("$.data.body.level").value("collaborative"))
                .andReturn();

        String nextPolicyId = JsonPath.read(updated.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/policies/" + policyId)
                        .cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("inactive"));

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/policies/" + nextPolicyId + "/deactivate")
                        .cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("inactive"));

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/policies/" + nextPolicyId + "/activate")
                        .cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/audit")
                        .cookie(admin)
                        .param("category", "config_change")
                        .param("objectId", nextPolicyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].action").value("policy.activate"))
                .andExpect(jsonPath("$.data.data[1].action").value("policy.deactivate"))
                .andExpect(jsonPath("$.data.data[2].action").value("policy.update"));
    }

    @Test
    void policyExceptionsAreDurableAndAudited() throws Exception {
        Cookie admin = loginCookie("43910516");

        MvcResult created = mockMvc.perform(post(ApiConstants.API_V1 + "/platform/policies/pol-release-approval/exceptions")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "One-off production release",
                                  "requesterId": "43910000",
                                  "approverId": "43910516",
                                  "expiresAt": "2099-01-01T00:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reason").value("One-off production release"))
                .andReturn();

        String exceptionId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(delete(ApiConstants.API_V1 + "/platform/policies/pol-release-approval/exceptions/" + exceptionId)
                        .cookie(admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/policies/pol-release-approval/exceptions")
                        .cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(exceptionId))
                .andExpect(jsonPath("$.data[0].revokedAt").isNotEmpty());

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/audit")
                        .cookie(admin)
                        .param("category", "config_change")
                        .param("objectId", "pol-release-approval"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].action").value("policy.exception.revoke"))
                .andExpect(jsonPath("$.data.data[1].action").value("policy.exception.add"));
    }

    @Test
    void invalidPolicyPayloadReturnsStableError() throws Exception {
        Cookie admin = loginCookie("43910516");

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/policies")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "bad-policy",
                                  "name": "Bad Policy",
                                  "category": "unknown",
                                  "scopeType": "platform",
                                  "scopeId": "*",
                                  "status": "active",
                                  "body": { "enabled": true }
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INVALID_POLICY_CATEGORY"));
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
