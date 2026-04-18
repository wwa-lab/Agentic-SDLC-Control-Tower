package com.sdlctower.domain.codebuildmanagement;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

public final class CodeBuildManagementConstants {

    private CodeBuildManagementConstants() {}

    public static final Duration PROJECTION_BUDGET = Duration.ofMillis(500);
    public static final Duration AGGREGATE_TOTAL_BUDGET = Duration.ofSeconds(2);
    public static final Instant REFERENCE_NOW = Instant.parse("2026-04-18T00:00:00Z");
    public static final Duration RECENT_WINDOW = Duration.ofDays(14);
    public static final Duration RESYNC_WINDOW = Duration.ofHours(72);
    public static final Duration BACKFILL_WINDOW = Duration.ofDays(14);
    public static final int LOG_EXCERPT_MAX_BYTES = 1_048_576;
    public static final int RECENT_RUNS_LIMIT = 25;
    public static final int RECENT_PRS_LIMIT = 15;
    public static final int RECENT_COMMITS_LIMIT = 20;
    public static final int COMBINED_LOG_TAIL_LINES = 200;
    public static final int RERUN_RATE_LIMIT_PER_HOUR = 5;
    public static final Duration STALE_WEBHOOK_THRESHOLD = Duration.ofMinutes(10);

    public static final Pattern WORKSPACE_ID_PATTERN = Pattern.compile("^ws-[a-z0-9\\-]+$");
    public static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^proj-[a-z0-9\\-]+$");
    public static final Pattern REPO_ID_PATTERN = Pattern.compile("^repo-[a-z0-9\\-]+$");
    public static final Pattern PR_ID_PATTERN = Pattern.compile("^pr-[a-z0-9\\-]+$");
    public static final Pattern RUN_ID_PATTERN = Pattern.compile("^run-[a-z0-9\\-]+$");
    public static final Pattern MEMBER_ID_PATTERN = Pattern.compile("^mem-[a-z0-9\\-]+$");
}
