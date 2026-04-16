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

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getCategory() { return category; }
    public String getSeverity() { return severity; }
    public String getSourceKind() { return sourceKind; }
    public String getSourceId() { return sourceId; }
    public String getTitle() { return title; }
    public String getDetail() { return detail; }
    public String getActionLabel() { return actionLabel; }
    public String getActionUrl() { return actionUrl; }
    public String getSkillName() { return skillName; }
    public String getExecutionId() { return executionId; }
    public Instant getDetectedAt() { return detectedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
}
