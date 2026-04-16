package com.sdlctower.domain.teamspace.dto;

import java.time.Instant;
import java.util.List;

public record MemberMatrixRowDto(
        String memberId,
        String displayName,
        List<String> roles,
        String oncallStatus,
        List<String> keyPermissions,
        Instant lastActiveAt
) {}
