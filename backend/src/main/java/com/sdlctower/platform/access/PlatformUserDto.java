package com.sdlctower.platform.access;

import java.time.Instant;

public record PlatformUserDto(
        String staffId,
        String displayName,
        String staffName,
        String avatarUrl,
        String email,
        String profileSource,
        Instant lastProfileSyncAt,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}
