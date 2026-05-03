package com.sdlctower.platform.workspace;

public class MissingWorkspaceContextException extends RuntimeException {
    public MissingWorkspaceContextException() {
        super("No workspace context set for the current thread");
    }
}
