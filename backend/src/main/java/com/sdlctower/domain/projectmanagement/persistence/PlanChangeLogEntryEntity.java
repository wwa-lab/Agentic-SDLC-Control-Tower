package com.sdlctower.domain.projectmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "plan_change_log_entries")
public class PlanChangeLogEntryEntity {

    @Id
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "actor_type", nullable = false)
    private String actorType;

    @Column(name = "actor_member_id")
    private String actorMemberId;

    @Column(name = "skill_execution_id")
    private String skillExecutionId;

    @Column(nullable = false)
    private String action;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Column(name = "before_json", columnDefinition = "CLOB")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "CLOB")
    private String afterJson;

    @Column
    private String reason;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "audit_link_id", nullable = false)
    private String auditLinkId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PlanChangeLogEntryEntity() {}

    public static PlanChangeLogEntryEntity create(
            String id,
            String projectId,
            String actorType,
            String actorMemberId,
            String skillExecutionId,
            String action,
            String targetType,
            String targetId,
            String beforeJson,
            String afterJson,
            String reason,
            String correlationId,
            String auditLinkId,
            Instant createdAt
    ) {
        PlanChangeLogEntryEntity entity = new PlanChangeLogEntryEntity();
        entity.id = id;
        entity.projectId = projectId;
        entity.actorType = actorType;
        entity.actorMemberId = actorMemberId;
        entity.skillExecutionId = skillExecutionId;
        entity.action = action;
        entity.targetType = targetType;
        entity.targetId = targetId;
        entity.beforeJson = beforeJson;
        entity.afterJson = afterJson;
        entity.reason = reason;
        entity.correlationId = correlationId;
        entity.auditLinkId = auditLinkId;
        entity.createdAt = createdAt;
        return entity;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getActorType() { return actorType; }
    public String getActorMemberId() { return actorMemberId; }
    public String getSkillExecutionId() { return skillExecutionId; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getBeforeJson() { return beforeJson; }
    public String getAfterJson() { return afterJson; }
    public String getReason() { return reason; }
    public String getCorrelationId() { return correlationId; }
    public String getAuditLinkId() { return auditLinkId; }
    public Instant getCreatedAt() { return createdAt; }
}
