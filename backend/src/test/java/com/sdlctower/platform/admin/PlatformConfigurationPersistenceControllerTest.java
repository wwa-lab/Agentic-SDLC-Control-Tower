package com.sdlctower.platform.admin;

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
class PlatformConfigurationPersistenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void templatesReadFromRepositoryAndUnknownTemplateReturnsNotFound() throws Exception {
        Cookie admin = loginCookie("43910516");

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/templates")
                        .cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].id").value("tpl-page-control"))
                .andExpect(jsonPath("$.data.data[0].version").value(1));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/templates/tpl-page-control")
                        .cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version.id").value("tpl-page-control-v1"))
                .andExpect(jsonPath("$.data.inheritance.layout.winningLayer").value("platform"));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/templates/missing-template")
                        .cookie(admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("TEMPLATE_NOT_FOUND"));
    }

    @Test
    void configurationLifecycleIsDurableAndAudited() throws Exception {
        Cookie admin = loginCookie("43910516");

        MvcResult created = mockMvc.perform(post(ApiConstants.API_V1 + "/platform/configurations")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "shell.nav.density",
                                  "kind": "component",
                                  "scopeType": "workspace",
                                  "scopeId": "ws-default-001",
                                  "parentId": "cfg-nav-density",
                                  "status": "active",
                                  "body": { "density": "compact" }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.key").value("shell.nav.density"))
                .andExpect(jsonPath("$.data.hasDrift").value(true))
                .andExpect(jsonPath("$.data.driftFields[0]").value("density"))
                .andReturn();

        String configId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(put(ApiConstants.API_V1 + "/platform/configurations/" + configId)
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "shell.nav.density",
                                  "kind": "component",
                                  "scopeType": "workspace",
                                  "scopeId": "ws-default-001",
                                  "parentId": "cfg-nav-density",
                                  "status": "active",
                                  "body": { "density": "comfortable" }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"))
                .andExpect(jsonPath("$.data.driftFields[0]").value("density"));

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/configurations")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "shell.nav.density",
                                  "kind": "component",
                                  "scopeType": "workspace",
                                  "scopeId": "ws-default-001",
                                  "status": "active",
                                  "body": { "density": "compact" }
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFIGURATION_ALREADY_EXISTS"));

        mockMvc.perform(put(ApiConstants.API_V1 + "/platform/configurations/" + configId)
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "shell.nav.density",
                                  "kind": "component",
                                  "scopeType": "workspace",
                                  "scopeId": "ws-default-001",
                                  "parentId": "cfg-nav-density",
                                  "status": "inactive",
                                  "body": { "density": "high" }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("inactive"))
                .andExpect(jsonPath("$.data.hasDrift").value(false));

        mockMvc.perform(get(ApiConstants.API_V1 + "/platform/audit")
                        .cookie(admin)
                        .param("category", "config_change")
                        .param("objectId", configId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].action").value("configuration.deactivate"))
                .andExpect(jsonPath("$.data.data[1].action").value("configuration.update"))
                .andExpect(jsonPath("$.data.data[2].action").value("configuration.create"));
    }

    @Test
    void invalidConfigurationPayloadReturnsStableError() throws Exception {
        Cookie admin = loginCookie("43910516");

        mockMvc.perform(post(ApiConstants.API_V1 + "/platform/configurations")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "bad.config",
                                  "kind": "unknown-kind",
                                  "scopeType": "platform",
                                  "scopeId": "*",
                                  "status": "active",
                                  "body": { "enabled": true }
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INVALID_CONFIG_KIND"));
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
