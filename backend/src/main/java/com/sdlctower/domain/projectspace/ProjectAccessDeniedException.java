package com.sdlctower.domain.projectspace;

public class ProjectAccessDeniedException extends RuntimeException {

    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}
