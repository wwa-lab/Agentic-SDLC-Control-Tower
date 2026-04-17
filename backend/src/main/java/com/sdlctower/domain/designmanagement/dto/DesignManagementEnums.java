package com.sdlctower.domain.designmanagement.dto;

import java.util.Locale;

public final class DesignManagementEnums {

    private DesignManagementEnums() {}

    public enum ArtifactLifecycle {
        DRAFT,
        PUBLISHED,
        RETIRED
    }

    public enum ArtifactFormat {
        STITCH,
        HTML
    }

    public enum CoverageStatus {
        OK,
        PARTIAL,
        STALE,
        MISSING,
        UNKNOWN
    }

    public enum CoverageDeclaration {
        FULL,
        PARTIAL
    }

    public enum AiSummaryStatus {
        PENDING,
        SUCCESS,
        FAILED
    }

    public enum ChangeLogEntryType {
        REGISTERED,
        VERSION_PUBLISHED,
        SPEC_LINKED,
        SPEC_UNLINKED,
        RETIRED,
        AI_SUMMARY_REGENERATED,
        LIFECYCLE_TRANSITIONED,
        METADATA_UPDATED
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
