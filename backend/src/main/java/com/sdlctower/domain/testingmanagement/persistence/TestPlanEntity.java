package com.sdlctower.domain.testingmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "test_plan")
public class TestPlanEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(name = "owner_member_id", nullable = false)
    private String ownerMemberId;

    @Column(nullable = false)
    private String state;

    @Column(name = "release_target")
    private String releaseTarget;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TestPlanEntity() {}

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getOwnerMemberId() { return ownerMemberId; }
    public String getState() { return state; }
    public String getReleaseTarget() { return releaseTarget; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
