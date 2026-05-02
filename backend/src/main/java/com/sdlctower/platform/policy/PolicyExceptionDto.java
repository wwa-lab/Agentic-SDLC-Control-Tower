package com.sdlctower.platform.policy;

import java.time.Instant;

public record PolicyExceptionDto(
        String id,
        String policyId,
        String reason,
        String requesterId,
        String approverId,
        Instant createdAt,
        Instant expiresAt,
        Instant revokedAt
) {}
