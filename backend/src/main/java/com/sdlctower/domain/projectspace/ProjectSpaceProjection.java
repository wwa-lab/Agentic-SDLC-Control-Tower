package com.sdlctower.domain.projectspace;

public interface ProjectSpaceProjection<T> {

    T load(String projectId);
}
