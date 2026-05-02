package com.sdlctower.platform.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "PLATFORM_WORKSPACE")
public class PlatformWorkspaceEntity {

    @Id
    private String id;

    @Column(name = "workspace_key", nullable = false)
    private String workspaceKey;

    @Column(nullable = false)
    private String name;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "snow_group_id", nullable = false)
    private String snowGroupId;

    @Column(name = "profile_id")
    private String profileId;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlatformWorkspaceEntity() {}

    public String getId() { return id; }
    public String getWorkspaceKey() { return workspaceKey; }
    public String getName() { return name; }
    public String getApplicationId() { return applicationId; }
    public String getSnowGroupId() { return snowGroupId; }
    public String getProfileId() { return profileId; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
