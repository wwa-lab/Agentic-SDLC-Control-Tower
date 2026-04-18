package com.sdlctower.domain.reportcenter.policy;

import com.sdlctower.domain.projectmanagement.policy.ProjectManagementActorResolver;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.platform.workspace.WorkspaceContextDto;
import com.sdlctower.platform.workspace.WorkspaceContextService;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import com.sdlctower.domain.teamspace.WorkspaceAccessDeniedException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ScopeAuthGuard {

    private final WorkspaceContextService workspaceContextService;
    private final TeamSpaceSeedCatalog teamSpaceSeedCatalog;
    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;
    private final ProjectManagementActorResolver actorResolver;

    public ScopeAuthGuard(
            WorkspaceContextService workspaceContextService,
            TeamSpaceSeedCatalog teamSpaceSeedCatalog,
            ProjectSpaceSeedCatalog projectSpaceSeedCatalog,
            ProjectManagementActorResolver actorResolver
    ) {
        this.workspaceContextService = workspaceContextService;
        this.teamSpaceSeedCatalog = teamSpaceSeedCatalog;
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
        this.actorResolver = actorResolver;
    }

    public ScopeContext authorize(String scope, List<String> requestedScopeIds, String orgId) {
        WorkspaceContextDto workspaceContext = workspaceContextService.getCurrentWorkspaceContext();
        ProjectManagementActorResolver.Actor actor = actorResolver.currentActor();

        if ("org".equals(scope)) {
            if (!(actor.auditor() || actor.isElevated())) {
                throw new WorkspaceAccessDeniedException("FORBIDDEN_SCOPE: org-viewer role is required");
            }
            if (requestedScopeIds.size() != 1 || !orgId.equals(requestedScopeIds.getFirst())) {
                throw new IllegalArgumentException("Expected exactly one org scope id: " + orgId);
            }
            return new ScopeContext(actor.memberId(), scope, List.copyOf(requestedScopeIds), orgId, workspaceContext);
        }

        if ("workspace".equals(scope)) {
            List<String> resolved = requestedScopeIds.stream()
                    .peek(id -> {
                        if (!teamSpaceSeedCatalog.exists(id)) {
                            throw new ResourceNotFoundException("Workspace " + id + " not found");
                        }
                    })
                    .filter(id -> actor.isElevated() || !"ws-private-001".equals(id))
                    .toList();
            if (resolved.size() != requestedScopeIds.size()) {
                throw new WorkspaceAccessDeniedException("FORBIDDEN_SCOPE: caller cannot access requested workspace scope");
            }
            return new ScopeContext(actor.memberId(), scope, resolved, orgId, workspaceContext);
        }

        if ("project".equals(scope)) {
            List<String> resolved = requestedScopeIds.stream()
                    .peek(id -> {
                        if (!projectSpaceSeedCatalog.exists(id)) {
                            throw new ResourceNotFoundException("Project " + id + " not found");
                        }
                    })
                    .filter(id -> actor.isElevated() || !"ws-private-001".equals(projectSpaceSeedCatalog.project(id).workspaceId()))
                    .toList();
            if (resolved.size() != requestedScopeIds.size()) {
                throw new WorkspaceAccessDeniedException("FORBIDDEN_SCOPE: caller cannot access requested project scope");
            }
            return new ScopeContext(actor.memberId(), scope, resolved, orgId, workspaceContext);
        }

        throw new IllegalArgumentException("Unsupported scope: " + scope);
    }

    public AccessMatrix listAccessibleScopes(String orgId) {
        ProjectManagementActorResolver.Actor actor = actorResolver.currentActor();
        Set<String> workspaces = new LinkedHashSet<>();
        for (String workspaceId : List.of("ws-default-001", "ws-legacy-001", "ws-degraded-001", "ws-private-001")) {
            if (actor.isElevated() || !"ws-private-001".equals(workspaceId)) {
                workspaces.add(workspaceId);
            }
        }

        Set<String> projects = new LinkedHashSet<>();
        projectSpaceSeedCatalog.projects().forEach(project -> {
            if (actor.isElevated() || !"ws-private-001".equals(project.workspaceId())) {
                projects.add(project.id());
            }
        });

        boolean orgViewer = actor.auditor() || actor.isElevated();
        return new AccessMatrix(actor.memberId(), orgViewer ? List.of(orgId) : List.of(), List.copyOf(workspaces), List.copyOf(projects));
    }

    public record AccessMatrix(
            String actorId,
            List<String> orgIds,
            List<String> workspaceIds,
            List<String> projectIds
    ) {}

    public record ScopeContext(
            String actorId,
            String scope,
            List<String> scopeIds,
            String orgId,
            WorkspaceContextDto workspaceContext
    ) {
        public boolean matches(String rowOrgId, String rowWorkspaceId, String rowProjectId) {
            return switch (scope) {
                case "org" -> orgId.equals(rowOrgId);
                case "workspace" -> scopeIds.contains(rowWorkspaceId);
                case "project" -> scopeIds.contains(rowProjectId);
                default -> false;
            };
        }
    }
}
