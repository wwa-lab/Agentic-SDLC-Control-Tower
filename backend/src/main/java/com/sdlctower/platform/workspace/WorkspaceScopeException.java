package com.sdlctower.platform.workspace;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class WorkspaceScopeException extends RuntimeException {
    public WorkspaceScopeException(String workspaceId) {
        super("User does not have scope on workspace: " + workspaceId);
    }
}
