package com.sdlctower.platform.auth;

import java.util.List;

public record CurrentUserDto(
        String mode,
        String authProvider,
        String staffId,
        String displayName,
        String staffName,
        String avatarUrl,
        List<String> roles,
        Boolean readOnly,
        List<ScopeDto> scopes
) {}
