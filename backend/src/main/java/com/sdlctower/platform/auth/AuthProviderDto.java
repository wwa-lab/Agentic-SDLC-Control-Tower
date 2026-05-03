package com.sdlctower.platform.auth;

public record AuthProviderDto(
        String provider,
        String label,
        Boolean enabled,
        String startUrl
) {}
