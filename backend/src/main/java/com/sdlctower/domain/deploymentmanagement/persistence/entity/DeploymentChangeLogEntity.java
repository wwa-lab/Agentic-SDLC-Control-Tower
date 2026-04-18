package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_change_log")
public class DeploymentChangeLogEntity {

    @Id private String id;
    @Column(name = "entity_type", nullable = false) private String entityType;
    @Column(name = "entity_id", nullable = false) private String entityId;
    @Column(name = "entry_type", nullable = false) private String entryType;
    @Column private String actor;
    @Column(length = 2048) private String detail;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected DeploymentChangeLogEntity() {}

    public static DeploymentChangeLogEntity create(String id, String entityType, String entityId,
                                                    String entryType, String actor, String detail) {
        var e = new DeploymentChangeLogEntity();
        e.id = id; e.entityType = entityType; e.entityId = entityId;
        e.entryType = entryType; e.actor = actor; e.detail = detail;
        e.createdAt = Instant.now();
        return e;
    }

    public String getId() { return id; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getEntryType() { return entryType; }
}
