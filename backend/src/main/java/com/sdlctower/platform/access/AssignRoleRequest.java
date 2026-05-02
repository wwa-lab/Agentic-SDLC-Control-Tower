package com.sdlctower.platform.access;

public record AssignRoleRequest(
        String staffId,
        String role,
        String scopeType,
        String scopeId
) {}
