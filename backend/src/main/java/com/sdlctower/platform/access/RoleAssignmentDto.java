package com.sdlctower.platform.access;

import java.time.Instant;

public record RoleAssignmentDto(
        String id,
        String staffId,
        String userDisplayName,
        String role,
        String scopeType,
        String scopeId,
        String grantedBy,
        Instant grantedAt
) {}
