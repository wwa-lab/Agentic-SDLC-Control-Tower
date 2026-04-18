package com.sdlctower.domain.codebuildmanagement.dto;

import java.util.Locale;

public final class CodeBuildManagementEnums {

    private CodeBuildManagementEnums() {}

    public enum RepoVisibility {
        PUBLIC,
        PRIVATE,
        INTERNAL
    }

    public enum PrState {
        OPEN,
        MERGED,
        CLOSED,
        DRAFT
    }

    public enum RunStatus {
        SUCCESS,
        FAILURE,
        IN_PROGRESS,
        QUEUED,
        CANCELLED,
        NEUTRAL
    }

    public enum RunTrigger {
        PUSH,
        PULL_REQUEST,
        SCHEDULE,
        WORKFLOW_DISPATCH,
        REPOSITORY_DISPATCH,
        WORKFLOW_CALL,
        OTHER
    }

    public enum StepConclusion {
        SUCCESS,
        FAILURE,
        SKIPPED,
        CANCELLED,
        TIMED_OUT,
        ACTION_REQUIRED,
        NEUTRAL
    }

    public enum StoryLinkStatus {
        KNOWN,
        UNKNOWN_STORY,
        NO_STORY_ID,
        AMBIGUOUS
    }

    public enum AiNoteSeverity {
        BLOCKER,
        MAJOR,
        MINOR,
        INFO
    }

    public enum AiRowStatus {
        SUCCESS,
        PENDING,
        FAILED,
        FAILED_EVIDENCE
    }

    public enum ChangeLogEntryType {
        REPO_SYNCED,
        PR_OPENED,
        PR_MERGED,
        PR_CLOSED,
        PR_HEAD_ADVANCED,
        RUN_COMPLETED,
        RUN_RERUN_REQUESTED,
        AI_PR_REVIEW_REGENERATED,
        AI_TRIAGE_REGENERATED,
        AI_SUMMARY_REGENERATED,
        INSTALLATION_LINKED,
        INSTALLATION_DEACTIVATED,
        BACKFILL_COMPLETED,
        RESYNC_COMPLETED
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
