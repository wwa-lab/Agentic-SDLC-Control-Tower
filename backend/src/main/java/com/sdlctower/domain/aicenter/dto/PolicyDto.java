package com.sdlctower.domain.aicenter.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PolicyDto(
        String skillKey,
        String autonomyLevel,
        List<String> approvalRequiredActions,
        List<String> authorizedApproverRoles,
        Map<String, Object> riskThresholds,
        Instant lastChangedAt,
        String lastChangedBy
) {}
