package com.sdlctower.domain.aicenter.dto;

import java.time.Instant;
import java.util.List;

public record RunFilter(
        List<String> skillKey,
        List<String> status,
        String triggerSourcePage,
        Instant startedAfter,
        Instant startedBefore,
        String triggeredByType
) {}
