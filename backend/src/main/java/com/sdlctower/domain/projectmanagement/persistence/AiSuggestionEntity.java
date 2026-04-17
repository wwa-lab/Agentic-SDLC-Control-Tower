package com.sdlctower.domain.projectmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ai_suggestions")
public class AiSuggestionEntity {

    @Id
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(nullable = false)
    private String kind;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Column(name = "payload_json", nullable = false, columnDefinition = "CLOB")
    private String payloadJson;

    @Column(nullable = false)
    private double confidence;

    @Column(nullable = false)
    private String state;

    @Column(name = "skill_execution_id")
    private String skillExecutionId;

    @Column(name = "suppress_until")
    private Instant suppressUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected AiSuggestionEntity() {}

    public static AiSuggestionEntity create(
            String id,
            String projectId,
            String kind,
            String targetType,
            String targetId,
            String payloadJson,
            double confidence,
            String state,
            String skillExecutionId,
            Instant suppressUntil,
            Instant createdAt,
            Instant updatedAt,
            Instant resolvedAt
    ) {
        AiSuggestionEntity entity = new AiSuggestionEntity();
        entity.id = id;
        entity.projectId = projectId;
        entity.kind = kind;
        entity.targetType = targetType;
        entity.targetId = targetId;
        entity.payloadJson = payloadJson;
        entity.confidence = confidence;
        entity.state = state;
        entity.skillExecutionId = skillExecutionId;
        entity.suppressUntil = suppressUntil;
        entity.createdAt = createdAt;
        entity.updatedAt = updatedAt;
        entity.resolvedAt = resolvedAt;
        return entity;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getKind() { return kind; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getPayloadJson() { return payloadJson; }
    public double getConfidence() { return confidence; }
    public String getState() { return state; }
    public String getSkillExecutionId() { return skillExecutionId; }
    public Instant getSuppressUntil() { return suppressUntil; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getResolvedAt() { return resolvedAt; }

    public void accept(Instant now) {
        this.state = "ACCEPTED";
        this.resolvedAt = now;
        this.updatedAt = now;
        this.suppressUntil = null;
    }

    public void dismiss(Instant suppressUntil, Instant now) {
        this.state = "DISMISSED";
        this.suppressUntil = suppressUntil;
        this.resolvedAt = now;
        this.updatedAt = now;
    }
}
