package com.sdlctower.domain.designmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "design_change_log")
public class DesignChangeLogEntity {

    @Id
    private String id;

    @Column(name = "artifact_id", nullable = false)
    private String artifactId;

    @Column(name = "entry_type", nullable = false)
    private String entryType;

    @Column(name = "actor_member_id")
    private String actorMemberId;

    @Column(name = "actor_skill_execution_id")
    private String actorSkillExecutionId;

    @Lob
    @Column(name = "before_json")
    private String beforeJson;

    @Lob
    @Column(name = "after_json")
    private String afterJson;

    @Lob
    private String reason;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected DesignChangeLogEntity() {}

    public static DesignChangeLogEntity create(
            String id,
            String artifactId,
            String entryType,
            String actorMemberId,
            String actorSkillExecutionId,
            String beforeJson,
            String afterJson,
            String reason,
            String correlationId,
            Instant occurredAt
    ) {
        DesignChangeLogEntity entity = new DesignChangeLogEntity();
        entity.id = id;
        entity.artifactId = artifactId;
        entity.entryType = entryType;
        entity.actorMemberId = actorMemberId;
        entity.actorSkillExecutionId = actorSkillExecutionId;
        entity.beforeJson = beforeJson;
        entity.afterJson = afterJson;
        entity.reason = reason;
        entity.correlationId = correlationId;
        entity.occurredAt = occurredAt;
        return entity;
    }

    public String getId() { return id; }
    public String getArtifactId() { return artifactId; }
    public String getEntryType() { return entryType; }
    public String getActorMemberId() { return actorMemberId; }
    public String getActorSkillExecutionId() { return actorSkillExecutionId; }
    public String getBeforeJson() { return beforeJson; }
    public String getAfterJson() { return afterJson; }
    public String getReason() { return reason; }
    public String getCorrelationId() { return correlationId; }
    public Instant getOccurredAt() { return occurredAt; }
}
