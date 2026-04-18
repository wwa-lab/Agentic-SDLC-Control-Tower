package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "code_build_change_log")
public class CodeBuildChangeLogEntity {

    @Id
    private String id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "entry_type", nullable = false)
    private String entryType;

    @Column(name = "actor_id")
    private String actorId;

    private String detail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CodeBuildChangeLogEntity() {}

    public static CodeBuildChangeLogEntity create(String id, String entityType, String entityId, String entryType, String actorId, String detail, Instant createdAt) {
        CodeBuildChangeLogEntity entity = new CodeBuildChangeLogEntity();
        entity.id = id;
        entity.entityType = entityType;
        entity.entityId = entityId;
        entity.entryType = entryType;
        entity.actorId = actorId;
        entity.detail = detail;
        entity.createdAt = createdAt;
        return entity;
    }

    public String getId() { return id; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getEntryType() { return entryType; }
    public String getActorId() { return actorId; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }
}
