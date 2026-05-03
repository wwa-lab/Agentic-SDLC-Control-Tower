package com.sdlctower.platform.access;

public record UpsertPlatformUserRequest(
        String staffId,
        String displayName,
        String staffName,
        String avatarUrl,
        String email,
        String profileSource,
        String status
) {}
