package com.sdlctower.domain.projectspace.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "project_dependencies")
public class ProjectDependencyEntity {

    @Id
    private String id;

    @Column(name = "source_project_id", nullable = false)
    private String sourceProjectId;

    @Column(name = "target_name", nullable = false)
    private String targetName;

    @Column(name = "target_ref", nullable = false)
    private String targetRef;

    @Column(name = "target_project_id")
    private String targetProjectId;

    @Column(nullable = false)
    private String direction;

    @Column(nullable = false)
    private String relationship;

    @Column(name = "owner_team", nullable = false)
    private String ownerTeam;

    @Column(nullable = false)
    private String health;

    @Column(name = "blocker_reason")
    private String blockerReason;

    @Column(nullable = false)
    private boolean external;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProjectDependencyEntity() {}

    public String getId() { return id; }
    public String getSourceProjectId() { return sourceProjectId; }
    public String getTargetName() { return targetName; }
    public String getTargetRef() { return targetRef; }
    public String getTargetProjectId() { return targetProjectId; }
    public String getDirection() { return direction; }
    public String getRelationship() { return relationship; }
    public String getOwnerTeam() { return ownerTeam; }
    public String getHealth() { return health; }
    public String getBlockerReason() { return blockerReason; }
    public boolean isExternal() { return external; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
