package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "requirement_sdd_document_index")
public class RequirementSddDocumentIndexEntity {
    @Id
    private String id;
    @Column(name = "requirement_id", nullable = false)
    private String requirementId;
    @Column(name = "profile_id", nullable = false)
    private String profileId;
    @Column(name = "sdd_workspace_id")
    private String sddWorkspaceId;
    @Column(name = "sdd_type", nullable = false)
    private String sddType;
    @Column(name = "stage_label", nullable = false)
    private String stageLabel;
    @Column(nullable = false)
    private String title;
    @Column(name = "repo_full_name", nullable = false)
    private String repoFullName;
    @Column(name = "branch_or_ref", nullable = false)
    private String branchOrRef;
    @Column(nullable = false)
    private String path;
    @Column(name = "latest_commit_sha", nullable = false)
    private String latestCommitSha;
    @Column(name = "latest_blob_sha", nullable = false)
    private String latestBlobSha;
    @Column(name = "github_url", nullable = false, length = 1024)
    private String githubUrl;
    @Column(nullable = false)
    private String status;
    @Column(name = "indexed_at", nullable = false)
    private Instant indexedAt;

    protected RequirementSddDocumentIndexEntity() {}

    public String getId() { return id; }
    public String getRequirementId() { return requirementId; }
    public String getProfileId() { return profileId; }
    public String getSddWorkspaceId() { return sddWorkspaceId; }
    public String getSddType() { return sddType; }
    public String getStageLabel() { return stageLabel; }
    public String getTitle() { return title; }
    public String getRepoFullName() { return repoFullName; }
    public String getBranchOrRef() { return branchOrRef; }
    public String getPath() { return path; }
    public String getLatestCommitSha() { return latestCommitSha; }
    public String getLatestBlobSha() { return latestBlobSha; }
    public String getGithubUrl() { return githubUrl; }
    public String getStatus() { return status; }
    public Instant getIndexedAt() { return indexedAt; }
}
