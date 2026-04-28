package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "project_sdd_workspace")
public class ProjectSddWorkspaceEntity {
    @Id
    private String id;
    @Column(name = "application_id", nullable = false)
    private String applicationId;
    @Column(name = "application_name", nullable = false)
    private String applicationName;
    @Column(name = "snow_group", nullable = false)
    private String snowGroup;
    @Column(name = "source_repo_full_name", nullable = false)
    private String sourceRepoFullName;
    @Column(name = "sdd_repo_full_name", nullable = false)
    private String sddRepoFullName;
    @Column(name = "base_branch", nullable = false)
    private String baseBranch;
    @Column(name = "working_branch", nullable = false)
    private String workingBranch;
    @Column(name = "lifecycle_status", nullable = false)
    private String lifecycleStatus;
    @Column(name = "docs_root", nullable = false)
    private String docsRoot;
    @Column(name = "release_pr_url")
    private String releasePrUrl;
    @Column(name = "kb_repo_full_name")
    private String kbRepoFullName;
    @Column(name = "kb_main_branch")
    private String kbMainBranch;
    @Column(name = "kb_preview_branch")
    private String kbPreviewBranch;
    @Column(name = "graph_manifest_path")
    private String graphManifestPath;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProjectSddWorkspaceEntity() {}

    public String getId() { return id; }
    public String getApplicationId() { return applicationId; }
    public String getApplicationName() { return applicationName; }
    public String getSnowGroup() { return snowGroup; }
    public String getSourceRepoFullName() { return sourceRepoFullName; }
    public String getSddRepoFullName() { return sddRepoFullName; }
    public String getBaseBranch() { return baseBranch; }
    public String getWorkingBranch() { return workingBranch; }
    public String getLifecycleStatus() { return lifecycleStatus; }
    public String getDocsRoot() { return docsRoot; }
    public String getReleasePrUrl() { return releasePrUrl; }
    public String getKbRepoFullName() { return kbRepoFullName; }
    public String getKbMainBranch() { return kbMainBranch; }
    public String getKbPreviewBranch() { return kbPreviewBranch; }
    public String getGraphManifestPath() { return graphManifestPath; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
