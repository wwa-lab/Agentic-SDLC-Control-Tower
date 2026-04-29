package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "requirement_artifact_link")
public class RequirementArtifactLinkEntity {
    @Id
    private String id;
    @Column(name = "execution_id", nullable = false)
    private String executionId;
    @Column(name = "requirement_id", nullable = false)
    private String requirementId;
    @Column(name = "artifact_type", nullable = false)
    private String artifactType;
    @Column(name = "storage_type", nullable = false)
    private String storageType;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 1024)
    private String uri;
    @Column(name = "repo_full_name")
    private String repoFullName;
    private String path;
    @Column(name = "commit_sha")
    private String commitSha;
    @Column(name = "blob_sha")
    private String blobSha;
    @Column(nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RequirementArtifactLinkEntity() {}

    public static RequirementArtifactLinkEntity create(String id, String executionId, String requirementId, String artifactType, String storageType, String title, String uri, String repoFullName, String path, String commitSha, String blobSha, String status, Instant createdAt) {
        RequirementArtifactLinkEntity entity = new RequirementArtifactLinkEntity();
        entity.id = id;
        entity.executionId = executionId;
        entity.requirementId = requirementId;
        entity.artifactType = artifactType;
        entity.storageType = storageType;
        entity.title = title;
        entity.uri = uri;
        entity.repoFullName = repoFullName;
        entity.path = path;
        entity.commitSha = commitSha;
        entity.blobSha = blobSha;
        entity.status = status;
        entity.createdAt = createdAt;
        return entity;
    }

    public String getId() { return id; }
    public String getExecutionId() { return executionId; }
    public String getRequirementId() { return requirementId; }
    public String getArtifactType() { return artifactType; }
    public String getStorageType() { return storageType; }
    public String getTitle() { return title; }
    public String getUri() { return uri; }
    public String getRepoFullName() { return repoFullName; }
    public String getPath() { return path; }
    public String getCommitSha() { return commitSha; }
    public String getBlobSha() { return blobSha; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
