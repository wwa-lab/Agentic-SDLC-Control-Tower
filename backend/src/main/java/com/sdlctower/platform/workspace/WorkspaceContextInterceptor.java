package com.sdlctower.platform.workspace;

import com.sdlctower.platform.auth.AuthProperties;
import com.sdlctower.platform.auth.AuthService;
import com.sdlctower.platform.auth.CurrentUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Resolves workspace context for every request under /api/v1/workspaces/{workspaceId}/**.
 * Allowlisted paths bypass workspace resolution entirely.
 * Cleared in afterCompletion to prevent holder leak across requests.
 */
@Component
public class WorkspaceContextInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceContextInterceptor.class);

    private static final String WORKSPACE_PREFIX = "/api/v1/workspaces/";
    private static final String DEMO_WORKSPACE_ID = "demo";

    private static final List<String> ALLOWLIST = List.of(
            "/api/v1/auth/",
            "/api/v1/health",
            "/api/v1/integration/webhooks/",
            "/api/v1/platform/",
            "/api/v1/reports/fleet/",
            "/api/v1/nav/",
            "/api/v1/shell/",
            "/api/v1/support/",
            "/api/v1/pipeline-profiles",
            "/api/v1/workspace-context"  // legacy single-row endpoint used by demo path
    );

    private final AuthService authService;
    private final WorkspaceContextResolver resolver;
    private final AuthProperties authProperties;

    public WorkspaceContextInterceptor(AuthService authService,
                                       WorkspaceContextResolver resolver,
                                       AuthProperties authProperties) {
        this.authService = authService;
        this.resolver = resolver;
        this.authProperties = authProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String path = req.getRequestURI();

        if (isAllowlisted(path)) {
            return true;
        }

        if (!path.startsWith(WORKSPACE_PREFIX)) {
            res.sendError(HttpStatus.NOT_FOUND.value(), "WORKSPACE_PREFIX_REQUIRED");
            return false;
        }

        String workspaceId = extractWorkspaceId(path);
        if (workspaceId == null || workspaceId.isBlank()) {
            res.sendError(HttpStatus.NOT_FOUND.value(), "WORKSPACE_PREFIX_REQUIRED");
            return false;
        }

        CurrentUserDto user = authService.fromRequest(req).orElse(null);

        if (DEMO_WORKSPACE_ID.equals(workspaceId)) {
            return handleDemoWorkspace(user, res);
        }

        if (user == null) {
            res.sendError(HttpStatus.UNAUTHORIZED.value(), "AUTH_REQUIRED");
            return false;
        }

        WorkspaceContext ctx;
        try {
            ctx = resolver.resolveById(workspaceId, false);
        } catch (WorkspaceNotFoundException e) {
            res.sendError(HttpStatus.NOT_FOUND.value(), "WORKSPACE_NOT_FOUND");
            return false;
        }

        if (!resolver.hasScope(user, workspaceId)) {
            log.warn("workspace.access_denied staffId={} workspaceId={}", user.staffId(), workspaceId);
            res.sendError(HttpStatus.FORBIDDEN.value(), "WORKSPACE_SCOPE_REQUIRED");
            return false;
        }

        WorkspaceContextHolder.set(ctx);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        WorkspaceContextHolder.clear();
    }

    private boolean handleDemoWorkspace(CurrentUserDto user, HttpServletResponse res) throws Exception {
        if (user != null && !"guest".equals(user.mode())) {
            res.sendError(HttpStatus.FORBIDDEN.value(), "WORKSPACE_SCOPE_REQUIRED");
            return false;
        }
        if (!authProperties.isDemoMode()) {
            res.sendError(HttpStatus.FORBIDDEN.value(), "DEMO_DISABLED");
            return false;
        }
        WorkspaceContextHolder.set(new WorkspaceContext(
                DEMO_WORKSPACE_ID, DEMO_WORKSPACE_ID, "Demo Workspace",
                "demo-app", "demo-snow", "standard-java-sdd", true));
        return true;
    }

    private boolean isAllowlisted(String path) {
        return ALLOWLIST.stream().anyMatch(path::startsWith);
    }

    private String extractWorkspaceId(String path) {
        int start = WORKSPACE_PREFIX.length();
        int end = path.indexOf('/', start);
        if (end == -1) end = path.length();
        return path.substring(start, end);
    }
}
