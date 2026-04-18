package com.sdlctower.domain.projectmanagement.policy;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementEnums;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementEnums.DependencyResolutionState;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementEnums.MilestoneStatus;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementEnums.RiskState;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TransitionPolicy {

    private final Map<MilestoneStatus, EnumSet<MilestoneStatus>> milestoneTransitions = new EnumMap<>(MilestoneStatus.class);
    private final Map<RiskState, EnumSet<RiskState>> riskTransitions = new EnumMap<>(RiskState.class);
    private final Map<DependencyResolutionState, EnumSet<DependencyResolutionState>> dependencyTransitions =
            new EnumMap<>(DependencyResolutionState.class);

    public TransitionPolicy() {
        milestoneTransitions.put(MilestoneStatus.NOT_STARTED, EnumSet.of(MilestoneStatus.IN_PROGRESS, MilestoneStatus.ARCHIVED));
        milestoneTransitions.put(MilestoneStatus.IN_PROGRESS, EnumSet.of(MilestoneStatus.COMPLETED, MilestoneStatus.AT_RISK, MilestoneStatus.ARCHIVED));
        milestoneTransitions.put(MilestoneStatus.AT_RISK, EnumSet.of(MilestoneStatus.IN_PROGRESS, MilestoneStatus.SLIPPED, MilestoneStatus.ARCHIVED));
        milestoneTransitions.put(MilestoneStatus.SLIPPED, EnumSet.of(MilestoneStatus.IN_PROGRESS, MilestoneStatus.ARCHIVED));
        milestoneTransitions.put(MilestoneStatus.COMPLETED, EnumSet.of(MilestoneStatus.ARCHIVED));
        milestoneTransitions.put(MilestoneStatus.ARCHIVED, EnumSet.noneOf(MilestoneStatus.class));

        riskTransitions.put(RiskState.IDENTIFIED, EnumSet.of(RiskState.ACKNOWLEDGED, RiskState.ESCALATED));
        riskTransitions.put(RiskState.ACKNOWLEDGED, EnumSet.of(RiskState.MITIGATING, RiskState.ESCALATED));
        riskTransitions.put(RiskState.MITIGATING, EnumSet.of(RiskState.RESOLVED, RiskState.ESCALATED));
        riskTransitions.put(RiskState.RESOLVED, EnumSet.noneOf(RiskState.class));
        riskTransitions.put(RiskState.ESCALATED, EnumSet.of(RiskState.ACKNOWLEDGED));

        dependencyTransitions.put(DependencyResolutionState.PROPOSED, EnumSet.of(DependencyResolutionState.NEGOTIATING));
        dependencyTransitions.put(
                DependencyResolutionState.NEGOTIATING,
                EnumSet.of(
                        DependencyResolutionState.APPROVED,
                        DependencyResolutionState.REJECTED,
                        DependencyResolutionState.AT_RISK
                )
        );
        dependencyTransitions.put(
                DependencyResolutionState.AT_RISK,
                EnumSet.of(DependencyResolutionState.NEGOTIATING, DependencyResolutionState.RESOLVED)
        );
        dependencyTransitions.put(DependencyResolutionState.APPROVED, EnumSet.of(DependencyResolutionState.RESOLVED));
        dependencyTransitions.put(DependencyResolutionState.REJECTED, EnumSet.noneOf(DependencyResolutionState.class));
        dependencyTransitions.put(DependencyResolutionState.RESOLVED, EnumSet.noneOf(DependencyResolutionState.class));
    }

    public void milestone(String from, String to) {
        MilestoneStatus fromStatus = ProjectManagementEnums.parse(MilestoneStatus.class, from, "milestone status");
        MilestoneStatus toStatus = ProjectManagementEnums.parse(MilestoneStatus.class, to, "milestone transition");
        if (!milestoneTransitions.getOrDefault(fromStatus, EnumSet.noneOf(MilestoneStatus.class)).contains(toStatus)) {
            throw ProjectManagementException.conflict(
                    "PM_INVALID_TRANSITION",
                    "Milestone transition from " + fromStatus + " to " + toStatus + " is not allowed"
            );
        }
    }

    public void risk(String from, String to) {
        RiskState fromState = ProjectManagementEnums.parse(RiskState.class, from, "risk state");
        RiskState toState = ProjectManagementEnums.parse(RiskState.class, to, "risk transition");
        if (!riskTransitions.getOrDefault(fromState, EnumSet.noneOf(RiskState.class)).contains(toState)) {
            throw ProjectManagementException.conflict(
                    "PM_INVALID_TRANSITION",
                    "Risk transition from " + fromState + " to " + toState + " is not allowed"
            );
        }
    }

    public void dependency(String from, String to) {
        DependencyResolutionState fromState =
                ProjectManagementEnums.parse(DependencyResolutionState.class, from, "dependency state");
        DependencyResolutionState toState =
                ProjectManagementEnums.parse(DependencyResolutionState.class, to, "dependency transition");
        if (!dependencyTransitions.getOrDefault(fromState, EnumSet.noneOf(DependencyResolutionState.class)).contains(toState)) {
            throw ProjectManagementException.conflict(
                    "PM_INVALID_TRANSITION",
                    "Dependency transition from " + fromState + " to " + toState + " is not allowed"
            );
        }
    }

    public void requireSlippageReason(String to, String reason) {
        MilestoneStatus target = ProjectManagementEnums.parse(MilestoneStatus.class, to, "milestone transition");
        if ((target == MilestoneStatus.AT_RISK || target == MilestoneStatus.SLIPPED)
                && (reason == null || reason.trim().length() < 10)) {
            throw ProjectManagementException.invalid(
                    "PM_SLIPPAGE_REASON_REQUIRED",
                    "Slippage reason must be at least 10 characters"
            );
        }
    }

    public void requireRecoveryTargetDate(String to, LocalDate newTargetDate) {
        MilestoneStatus target = ProjectManagementEnums.parse(MilestoneStatus.class, to, "milestone transition");
        if (target == MilestoneStatus.IN_PROGRESS
                && newTargetDate != null
                && newTargetDate.isBefore(ProjectManagementConstants.REFERENCE_NOW.atZone(java.time.ZoneOffset.UTC).toLocalDate())) {
            throw ProjectManagementException.invalid(
                    "PM_VALIDATION_ERROR",
                    "newTargetDate must not be before today"
            );
        }
    }

    public void requireMitigationNote(String to, String mitigationNote) {
        RiskState target = ProjectManagementEnums.parse(RiskState.class, to, "risk transition");
        if (target == RiskState.MITIGATING && (mitigationNote == null || mitigationNote.trim().length() < 20)) {
            throw ProjectManagementException.invalid(
                    "PM_MITIGATION_NOTE_REQUIRED",
                    "Mitigation note must be at least 20 characters"
            );
        }
    }

    public void requireResolutionNote(String to, String resolutionNote) {
        RiskState target = ProjectManagementEnums.parse(RiskState.class, to, "risk transition");
        if (target == RiskState.RESOLVED && (resolutionNote == null || resolutionNote.trim().length() < 10)) {
            throw ProjectManagementException.invalid(
                    "PM_RESOLUTION_NOTE_REQUIRED",
                    "Resolution note must be at least 10 characters"
            );
        }
    }

    public void requireIncidentLink(String to, String incidentId) {
        RiskState target = ProjectManagementEnums.parse(RiskState.class, to, "risk transition");
        if (target == RiskState.ESCALATED && (incidentId == null || incidentId.isBlank())) {
            throw ProjectManagementException.invalid(
                    "PM_INCIDENT_LINK_REQUIRED",
                    "Escalated risks require a linked incident id"
            );
        }
    }

    public void requireDependencyCommitment(boolean external, String to, String commitment) {
        DependencyResolutionState target =
                ProjectManagementEnums.parse(DependencyResolutionState.class, to, "dependency transition");
        if (external && target == DependencyResolutionState.APPROVED && (commitment == null || commitment.isBlank())) {
            throw ProjectManagementException.invalid(
                    "PM_DEP_CONTRACT_REQUIRED",
                    "External dependency approval requires a contract commitment"
            );
        }
    }

    public void requireDependencyRejectionReason(String to, String rejectionReason) {
        DependencyResolutionState target =
                ProjectManagementEnums.parse(DependencyResolutionState.class, to, "dependency transition");
        if (target == DependencyResolutionState.REJECTED
                && (rejectionReason == null || rejectionReason.trim().length() < 10)) {
            throw ProjectManagementException.invalid(
                    "PM_DEP_REJECTION_REASON_REQUIRED",
                    "Rejected dependencies require a rejection reason"
            );
        }
    }

    public void requireOverallocationJustification(int totalPercent, String justification, String memberId) {
        if (totalPercent > 100 && (justification == null || justification.isBlank())) {
            throw ProjectManagementException.invalid(
                    "PM_OVERALLOCATION_JUSTIFICATION_REQUIRED",
                    "Overallocation above 100% requires justification for member " + memberId
            );
        }
    }
}
