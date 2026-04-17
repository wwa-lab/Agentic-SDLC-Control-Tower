package com.sdlctower.domain.designmanagement;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

public final class DesignManagementConstants {

    private DesignManagementConstants() {}

    public static final Duration PROJECTION_BUDGET = Duration.ofMillis(500);
    public static final Duration AGGREGATE_TOTAL_BUDGET = Duration.ofSeconds(2);
    public static final Instant REFERENCE_NOW = Instant.parse("2026-04-17T12:00:00Z");
    public static final long HTML_SIZE_LIMIT_BYTES = 2_097_152L;
    public static final String DEFAULT_SKILL_VERSION = "artifact-summarizer@1.0.0";

    public static final Pattern ARTIFACT_ID_PATTERN = Pattern.compile("^art-[a-z0-9\\-]+$");
    public static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^proj-[a-z0-9\\-]+$");
    public static final Pattern WORKSPACE_ID_PATTERN = Pattern.compile("^ws-[a-z0-9\\-]+$");
    public static final Pattern SPEC_ID_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]+$");

    public static long sizeBytes(String payload) {
        return payload == null ? 0 : payload.getBytes(StandardCharsets.UTF_8).length;
    }
}
