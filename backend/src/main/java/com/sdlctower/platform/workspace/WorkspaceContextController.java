package com.sdlctower.platform.workspace;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspace-context")
public class WorkspaceContextController {

    private final WorkspaceContextService workspaceContextService;

    public WorkspaceContextController(WorkspaceContextService workspaceContextService) {
        this.workspaceContextService = workspaceContextService;
    }

    @GetMapping
    public WorkspaceContext getWorkspaceContext() {
        return workspaceContextService.getCurrentWorkspaceContext();
    }
}
