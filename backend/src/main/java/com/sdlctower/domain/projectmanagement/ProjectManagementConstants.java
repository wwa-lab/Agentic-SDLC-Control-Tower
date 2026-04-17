package com.sdlctower.domain.projectmanagement;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

public final class ProjectManagementConstants {

    private ProjectManagementConstants() {}

    public static final Duration PROJECTION_BUDGET = Duration.ofMillis(500);
    public static final Duration AGGREGATE_TOTAL_BUDGET = Duration.ofSeconds(2);
    public static final Instant REFERENCE_NOW = Instant.parse("2026-04-17T12:00:00Z");

    public static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^proj-[a-z0-9\\-]+$");
    public static final Pattern WORKSPACE_ID_PATTERN = Pattern.compile("^ws-[a-z0-9\\-]+$");

    public static final int DEFAULT_CHANGE_LOG_PAGE_SIZE = 50;
    public static final int MAX_CHANGE_LOG_PAGE_SIZE = 200;
    public static final int CAPACITY_WARNING_THRESHOLD = 50;
    public static final String DEFAULT_AUTONOMY_LEVEL = "L2";
}
