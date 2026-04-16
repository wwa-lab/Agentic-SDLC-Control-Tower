package com.sdlctower.domain.dashboard.dto;

import java.util.List;

public record RecentActivityDto(
        List<ActivityEntryDto> entries,
        int total
) {
}
