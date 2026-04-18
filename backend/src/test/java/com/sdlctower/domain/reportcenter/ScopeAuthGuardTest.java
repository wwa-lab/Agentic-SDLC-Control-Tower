package com.sdlctower.domain.reportcenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sdlctower.domain.reportcenter.config.ReportCenterProperties;
import com.sdlctower.domain.reportcenter.policy.ScopeAuthGuard;
import com.sdlctower.domain.teamspace.WorkspaceAccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class ScopeAuthGuardTest {

    @Autowired
    private ScopeAuthGuard scopeAuthGuard;

    @Autowired
    private ReportCenterProperties properties;

    @Test
    void authorizeAllowsWorkspaceScopeForDefaultWorkspace() {
        ScopeAuthGuard.ScopeContext context = scopeAuthGuard.authorize("workspace", java.util.List.of("ws-default-001"), properties.orgId());
        assertEquals("workspace", context.scope());
        assertEquals(1, context.scopeIds().size());
    }

    @Test
    void authorizeRejectsPrivateWorkspaceWithoutElevation() {
        assertThrows(WorkspaceAccessDeniedException.class, () ->
                scopeAuthGuard.authorize("workspace", java.util.List.of("ws-private-001"), properties.orgId()));
    }
}
