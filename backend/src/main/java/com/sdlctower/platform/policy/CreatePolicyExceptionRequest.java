package com.sdlctower.platform.policy;

import java.time.Instant;

public record CreatePolicyExceptionRequest(
        String reason,
        String requesterId,
        String approverId,
        Instant expiresAt
) {}
