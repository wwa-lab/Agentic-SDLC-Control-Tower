package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import com.sdlctower.shared.persistence.WorkspacePrePersistListener;

@Entity
@Table(name = "requirement_source_reference")
@EntityListeners(WorkspacePrePersistListener.class)
public class RequirementSourceReferenceEntity {
    @Id
    private String id;
    @Column(name = "requirement_id", nullable = false)
    private String requirementId;
    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;
    @Column(name = "source_type", nullable = false)
    private String sourceType;
    @Column(name = "external_id")
    private String externalId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 1024)
    private String url;
    @Column(name = "source_updated_at")
    private Instant sourceUpdatedAt;
    @Column(name = "fetched_at")
    private Instant fetchedAt;
    @Column(name = "freshness_status", nullable = false)
    private String freshnessStatus;
    @Column(name = "error_message", columnDefinition = "CLOB")
    private String errorMessage;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RequirementSourceReferenceEntity() {}

    public static RequirementSourceReferenceEntity create(String id, String requirementId, String sourceType, String externalId, String title, String url, Instant sourceUpdatedAt, Instant fetchedAt, String freshnessStatus, String errorMessage, Instant createdAt) {
        RequirementSourceReferenceEntity entity = new RequirementSourceReferenceEntity();
        entity.id = id;
        entity.requirementId = requirementId;
        entity.sourceType = sourceType;
        entity.externalId = externalId;
        entity.title = title;
        entity.url = url;
        entity.sourceUpdatedAt = sourceUpdatedAt;
        entity.fetchedAt = fetchedAt;
        entity.freshnessStatus = freshnessStatus;
        entity.errorMessage = errorMessage;
        entity.createdAt = createdAt;
        return entity;
    }

    public void refresh(Instant sourceUpdatedAt, Instant fetchedAt, String freshnessStatus, String errorMessage) {
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.fetchedAt = fetchedAt;
        this.freshnessStatus = freshnessStatus;
        this.errorMessage = errorMessage;
    }

    public void refreshMetadata(String externalId, String title, Instant sourceUpdatedAt, Instant fetchedAt, String freshnessStatus, String errorMessage) {
        if (externalId != null && !externalId.isBlank()) {
            this.externalId = externalId;
        }
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        refresh(sourceUpdatedAt, fetchedAt, freshnessStatus, errorMessage);
    }

    public String getId() { return id; }
    public String getRequirementId() { return requirementId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getSourceType() { return sourceType; }
    public String getExternalId() { return externalId; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public Instant getSourceUpdatedAt() { return sourceUpdatedAt; }
    public Instant getFetchedAt() { return fetchedAt; }
    public String getFreshnessStatus() { return freshnessStatus; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
}
