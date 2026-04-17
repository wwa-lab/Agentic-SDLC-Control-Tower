package com.sdlctower.domain.designmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "design_artifact")
public class DesignArtifactEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String format;

    @Column(nullable = false)
    private String lifecycle;

    @Column(name = "current_version_id")
    private String currentVersionId;

    @Column(name = "registered_by_member_id", nullable = false)
    private String registeredByMemberId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DesignArtifactEntity() {}

    public static DesignArtifactEntity create(
            String id,
            String workspaceId,
            String projectId,
            String title,
            String format,
            String lifecycle,
            String currentVersionId,
            String registeredByMemberId,
            Instant createdAt,
            Instant updatedAt
    ) {
        DesignArtifactEntity entity = new DesignArtifactEntity();
        entity.id = id;
        entity.workspaceId = workspaceId;
        entity.projectId = projectId;
        entity.title = title;
        entity.format = format;
        entity.lifecycle = lifecycle;
        entity.currentVersionId = currentVersionId;
        entity.registeredByMemberId = registeredByMemberId;
        entity.createdAt = createdAt;
        entity.updatedAt = updatedAt;
        return entity;
    }

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getTitle() { return title; }
    public String getFormat() { return format; }
    public String getLifecycle() { return lifecycle; }
    public String getCurrentVersionId() { return currentVersionId; }
    public String getRegisteredByMemberId() { return registeredByMemberId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setLifecycle(String lifecycle) {
        this.lifecycle = lifecycle;
    }

    public void setCurrentVersionId(String currentVersionId) {
        this.currentVersionId = currentVersionId;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
