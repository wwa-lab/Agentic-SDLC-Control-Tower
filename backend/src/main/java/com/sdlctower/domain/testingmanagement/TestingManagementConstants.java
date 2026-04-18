package com.sdlctower.domain.testingmanagement;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

public final class TestingManagementConstants {

    private TestingManagementConstants() {}

    public static final Duration PROJECTION_BUDGET = Duration.ofMillis(500);
    public static final Duration AGGREGATE_TOTAL_BUDGET = Duration.ofSeconds(2);
    public static final Instant REFERENCE_NOW = Instant.parse("2026-04-18T00:00:00Z");
    public static final Duration RECENT_WINDOW = Duration.ofDays(7);
    public static final int FAILURE_EXCERPT_LIMIT = 4096;

    public static final Pattern WORKSPACE_ID_PATTERN = Pattern.compile("^ws-[a-z0-9\\-]+$");
    public static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^proj-[a-z0-9\\-]+$");
    public static final Pattern PLAN_ID_PATTERN = Pattern.compile("^plan-[a-z0-9\\-]+$");
    public static final Pattern CASE_ID_PATTERN = Pattern.compile("^case-[a-z0-9\\-]+$");
    public static final Pattern RUN_ID_PATTERN = Pattern.compile("^run-[a-z0-9\\-]+$");
    public static final Pattern ENV_ID_PATTERN = Pattern.compile("^env-[a-z0-9\\-]+$");
}
