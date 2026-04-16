package com.sdlctower.domain.teamspace;

@FunctionalInterface
public interface TeamSpaceProjection<T> {

    T load(String workspaceId);
}
