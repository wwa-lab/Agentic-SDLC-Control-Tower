package com.sdlctower.domain.deploymentmanagement.dto;

import java.util.Locale;

public final class DeploymentEnums {

    private DeploymentEnums() {}

    public enum DeployState {
        PENDING, IN_PROGRESS, SUCCEEDED, FAILED, CANCELLED, ROLLED_BACK
    }

    public enum DeployTrigger {
        PUSH_TO_MAIN, MANUAL, SCHEDULED,
        PROMOTE_FROM_DEV, PROMOTE_FROM_TEST, PROMOTE_FROM_STAGING,
        ROLLBACK
    }

    public enum DeployStageState {
        SUCCESS, FAILURE, ABORTED, UNSTABLE, SKIPPED, IN_PROGRESS, NOT_STARTED
    }

    public enum ReleaseState {
        PREPARED, DEPLOYED, SUPERSEDED, ABANDONED
    }

    public enum EnvironmentKind {
        DEV, TEST, STAGING, PROD, CUSTOM
    }

    public enum ApprovalDecision {
        APPROVED, REJECTED, TIMED_OUT
    }

    public enum ApprovalState {
        PROMPTED, APPROVED, REJECTED, TIMED_OUT
    }

    public enum AiRowStatus {
        PENDING, SUCCESS, FAILED, STALE, SUPERSEDED, EVIDENCE_MISMATCH, SKIPPED
    }

    public enum HealthLed {
        GREEN, AMBER, RED, UNKNOWN
    }

    public enum ChangeLogEntryType {
        APPLICATION_REGISTERED, DEPLOY_INGESTED, RELEASE_CREATED,
        APPROVAL_RECORDED, ROLLBACK_DETECTED,
        RELEASE_NOTES_GENERATED, SUMMARY_GENERATED,
        INSTANCE_BACKFILLED, RESYNC_DRIFT_HEALED
    }

    public enum RollbackDetectionSignal {
        TRIGGER_ROLLBACK, VERSION_OLDER_THAN_PRIOR
    }

    public enum OutboxStatus {
        PENDING, PROCESSING, DONE, FAILED
    }

    public enum JenkinsEventType {
        JOB_STARTED, JOB_COMPLETED, STAGE_COMPLETED,
        INPUT_APPROVED, INPUT_REJECTED, INPUT_TIMED_OUT,
        BUILD_PROMOTED, BUILD_DELETED
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
