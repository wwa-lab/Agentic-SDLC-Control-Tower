package com.sdlctower.domain.teamspace;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

public final class TeamSpaceConstants {

    private TeamSpaceConstants() {}

    public static final Duration PROJECTION_BUDGET = Duration.ofMillis(500);
    public static final Duration AGGREGATE_TOTAL_BUDGET = Duration.ofMillis(2_000);
    public static final Instant REFERENCE_NOW = Instant.parse("2026-04-17T12:00:00Z");
    public static final Pattern WORKSPACE_ID_PATTERN = Pattern.compile("^ws-[a-z0-9\\-]+$");
}
