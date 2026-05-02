package com.sdlctower.platform.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String staffId,
        @NotBlank String password
) {}
