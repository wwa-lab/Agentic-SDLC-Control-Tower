package com.sdlctower.domain.projectmanagement.dto;

import java.util.Locale;

public final class ProjectManagementEnums {

    private ProjectManagementEnums() {}

    public enum MilestoneStatus {
        NOT_STARTED,
        IN_PROGRESS,
        AT_RISK,
        COMPLETED,
        SLIPPED,
        ARCHIVED
    }

    public enum SlippageRiskScore {
        LOW,
        MEDIUM,
        HIGH,
        NONE
    }

    public enum RiskSeverity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    public enum RiskCategory {
        TECHNICAL,
        SECURITY,
        DELIVERY,
        DEPENDENCY,
        GOVERNANCE
    }

    public enum RiskState {
        IDENTIFIED,
        ACKNOWLEDGED,
        MITIGATING,
        RESOLVED,
        ESCALATED
    }

    public enum DependencyRelationship {
        API,
        DATA,
        SCHEDULE,
        SLA
    }

    public enum DependencyDirection {
        UPSTREAM,
        DOWNSTREAM
    }

    public enum DependencyResolutionState {
        PROPOSED,
        NEGOTIATING,
        APPROVED,
        REJECTED,
        AT_RISK,
        RESOLVED
    }

    public enum HealthIndicator {
        GREEN,
        YELLOW,
        RED,
        UNKNOWN
    }

    public enum AiSuggestionKind {
        SLIPPAGE,
        REBALANCE,
        MITIGATION,
        DEP_RESOLUTION
    }

    public enum AiSuggestionState {
        PENDING,
        ACCEPTED,
        DISMISSED
    }

    public enum PlanActorType {
        HUMAN,
        AI
    }

    public enum PlanAction {
        CREATE,
        UPDATE,
        TRANSITION,
        ARCHIVE,
        COUNTERSIGN,
        ESCALATE,
        ACCEPT_AI_SUGGESTION,
        DISMISS_AI_SUGGESTION
    }

    public enum PlanTargetType {
        MILESTONE,
        RISK,
        DEPENDENCY,
        CAPACITY_ALLOCATION,
        AI_SUGGESTION
    }

    public enum CadenceTrend {
        UP,
        DOWN,
        FLAT
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
