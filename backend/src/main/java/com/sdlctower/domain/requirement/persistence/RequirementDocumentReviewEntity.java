package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import com.sdlctower.shared.persistence.WorkspacePrePersistListener;

@Entity
@Table(name = "requirement_document_review")
@EntityListeners(WorkspacePrePersistListener.class)
public class RequirementDocumentReviewEntity {
    @Id
    private String id;
    @Column(name = "document_id", nullable = false)
    private String documentId;
    @Column(name = "requirement_id", nullable = false)
    private String requirementId;
    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;
    @Column(nullable = false)
    private String decision;
    @Column(columnDefinition = "CLOB")
    private String comment;
    @Column(name = "reviewer_id", nullable = false)
    private String reviewerId;
    @Column(name = "reviewer_type", nullable = false)
    private String reviewerType;
    @Column(name = "commit_sha", nullable = false)
    private String commitSha;
    @Column(name = "blob_sha", nullable = false)
    private String blobSha;
    @Column(name = "anchor_type")
    private String anchorType;
    @Column(name = "anchor_value")
    private String anchorValue;
    @Column(nullable = false)
    private boolean stale;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RequirementDocumentReviewEntity() {}

    public static RequirementDocumentReviewEntity create(String id, String documentId, String requirementId, String decision, String comment, String reviewerId, String reviewerType, String commitSha, String blobSha, String anchorType, String anchorValue, boolean stale, Instant createdAt) {
        RequirementDocumentReviewEntity entity = new RequirementDocumentReviewEntity();
        entity.id = id;
        entity.documentId = documentId;
        entity.requirementId = requirementId;
        entity.decision = decision;
        entity.comment = comment;
        entity.reviewerId = reviewerId;
        entity.reviewerType = reviewerType;
        entity.commitSha = commitSha;
        entity.blobSha = blobSha;
        entity.anchorType = anchorType;
        entity.anchorValue = anchorValue;
        entity.stale = stale;
        entity.createdAt = createdAt;
        return entity;
    }

    public String getId() { return id; }
    public String getDocumentId() { return documentId; }
    public String getRequirementId() { return requirementId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getDecision() { return decision; }
    public String getComment() { return comment; }
    public String getReviewerId() { return reviewerId; }
    public String getReviewerType() { return reviewerType; }
    public String getCommitSha() { return commitSha; }
    public String getBlobSha() { return blobSha; }
    public String getAnchorType() { return anchorType; }
    public String getAnchorValue() { return anchorValue; }
    public boolean isStale() { return stale; }
    public Instant getCreatedAt() { return createdAt; }
}
