package com.sdlctower.domain.teamspace.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "risk_signals")
public class RiskSignalEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "project_id")
    private String projectId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String severity;

    @Column(name = "source_kind", nullable = false)
    private String sourceKind;

    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @Column(nullable = false)
    private String title;

    @Column(name = "pm_state")
    private String pmState;

    @Column(name = "owner_member_id")
    private String ownerMemberId;

    @Column(name = "mitigation_note", columnDefinition = "CLOB")
    private String mitigationNote;

    @Column(name = "resolution_note", columnDefinition = "CLOB")
    private String resolutionNote;

    @Column(name = "escalated_incident_id")
    private String escalatedIncidentId;

    @Column(name = "plan_revision_at_update")
    private long planRevisionAtUpdate;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String detail;

    @Column(name = "action_label", nullable = false)
    private String actionLabel;

    @Column(name = "action_url", nullable = false)
    private String actionUrl;

    @Column(name = "skill_name")
    private String skillName;

    @Column(name = "execution_id")
    private String executionId;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected RiskSignalEntity() {}

    public static RiskSignalEntity create(
            String id,
            String workspaceId,
            String projectId,
            String category,
            String severity,
            String sourceKind,
            String sourceId,
            String title,
            String detail,
            String actionLabel,
            String actionUrl,
            String skillName,
            String executionId,
            Instant detectedAt,
            Instant resolvedAt
    ) {
        RiskSignalEntity entity = new RiskSignalEntity();
        entity.id = id;
        entity.workspaceId = workspaceId;
        entity.projectId = projectId;
        entity.category = category;
        entity.severity = severity;
        entity.sourceKind = sourceKind;
        entity.sourceId = sourceId;
        entity.title = title;
        entity.detail = detail;
        entity.actionLabel = actionLabel;
        entity.actionUrl = actionUrl;
        entity.skillName = skillName;
        entity.executionId = executionId;
        entity.detectedAt = detectedAt;
        entity.resolvedAt = resolvedAt;
        return entity;
    }

    public static RiskSignalEntity create(
            String id,
            String workspaceId,
            String category,
            String severity,
            String sourceKind,
            String sourceId,
            String title,
            String detail,
            String actionLabel,
            String actionUrl,
            String skillName,
            String executionId,
            Instant detectedAt,
            Instant resolvedAt
    ) {
        return create(
                id,
                workspaceId,
                null,
                category,
                severity,
                sourceKind,
                sourceId,
                title,
                detail,
                actionLabel,
                actionUrl,
                skillName,
                executionId,
                detectedAt,
                resolvedAt
        );
    }

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getCategory() { return category; }
    public String getSeverity() { return severity; }
    public String getSourceKind() { return sourceKind; }
    public String getSourceId() { return sourceId; }
    public String getTitle() { return title; }
    public String getPmState() { return pmState; }
    public String getOwnerMemberId() { return ownerMemberId; }
    public String getMitigationNote() { return mitigationNote; }
    public String getResolutionNote() { return resolutionNote; }
    public String getEscalatedIncidentId() { return escalatedIncidentId; }
    public long getPlanRevisionAtUpdate() { return planRevisionAtUpdate; }
    public String getDetail() { return detail; }
    public String getActionLabel() { return actionLabel; }
    public String getActionUrl() { return actionUrl; }
    public String getSkillName() { return skillName; }
    public String getExecutionId() { return executionId; }
    public Instant getDetectedAt() { return detectedAt; }
    public Instant getResolvedAt() { return resolvedAt; }

    public void setTitle(String title) { this.title = title; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setCategory(String category) { this.category = category; }
    public void setPmState(String pmState) { this.pmState = pmState; }
    public void setOwnerMemberId(String ownerMemberId) { this.ownerMemberId = ownerMemberId; }
    public void setMitigationNote(String mitigationNote) { this.mitigationNote = mitigationNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
    public void setEscalatedIncidentId(String escalatedIncidentId) { this.escalatedIncidentId = escalatedIncidentId; }
    public void setPlanRevisionAtUpdate(long planRevisionAtUpdate) { this.planRevisionAtUpdate = planRevisionAtUpdate; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
