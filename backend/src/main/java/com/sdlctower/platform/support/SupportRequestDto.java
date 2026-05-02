package com.sdlctower.platform.support;

import java.util.Map;

public record SupportRequestDto(
        String title,
        String category,
        String description,
        String route,
        Map<String, Object> context,
        String reporterStaffId,
        String reporterMode
) {}
