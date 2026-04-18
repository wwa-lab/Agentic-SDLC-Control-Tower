package com.sdlctower.domain.testingmanagement.dto;

import java.util.Locale;

public final class TestingManagementEnums {

    private TestingManagementEnums() {}

    public enum TestPlanState {
        DRAFT,
        ACTIVE,
        ARCHIVED
    }

    public enum TestCaseType {
        FUNCTIONAL,
        REGRESSION,
        SMOKE,
        PERF,
        SECURITY
    }

    public enum TestCasePriority {
        P0,
        P1,
        P2,
        P3
    }

    public enum TestCaseState {
        ACTIVE,
        DRAFT,
        DEPRECATED
    }

    public enum TestRunState {
        RUNNING,
        PASSED,
        FAILED,
        ABORTED,
        INGEST_FAILED
    }

    public enum TestResultOutcome {
        PASS,
        FAIL,
        SKIP,
        ERROR
    }

    public enum TestEnvironmentKind {
        DEV,
        STAGING,
        PROD,
        EPHEMERAL,
        OTHER
    }

    public enum RunTriggerSource {
        MANUAL_UPLOAD,
        CI_WEBHOOK
    }

    public enum CoverageStatus {
        GREEN,
        AMBER,
        RED,
        GREY
    }

    public enum ReqLinkStatus {
        VERIFIED,
        UNKNOWN_REQ,
        UNVERIFIED
    }

    public enum DraftOrigin {
        AI_DRAFT,
        MANUAL,
        IMPORTED
    }

    public static <E extends Enum<E>> E parse(Class<E> enumType, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported " + fieldName + ": " + value);
        }
    }
}
