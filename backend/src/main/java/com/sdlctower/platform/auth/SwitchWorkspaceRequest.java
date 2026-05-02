package com.sdlctower.platform.auth;

import jakarta.validation.constraints.NotBlank;

public record SwitchWorkspaceRequest(@NotBlank String workspaceId) {}
