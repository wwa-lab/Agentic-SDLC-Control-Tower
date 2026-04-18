package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "repo")
public class RepoEntity {

    @Id
    private String id;

    @Column(name = "github_repo_id")
    private Long githubRepoId;

    @Column(name = "installation_id")
    private String installationId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "default_branch")
    private String defaultBranch;

    @Column(name = "primary_language")
    private String primaryLanguage;

    @Column(length = 1024)
    private String description;

    private String visibility;

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RepoEntity() {}

    public String getId() { return id; }
    public Long getGithubRepoId() { return githubRepoId; }
    public String getInstallationId() { return installationId; }
    public String getFullName() { return fullName; }
    public String getProjectId() { return projectId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getDefaultBranch() { return defaultBranch; }
    public String getPrimaryLanguage() { return primaryLanguage; }
    public String getDescription() { return description; }
    public String getVisibility() { return visibility; }
    public String getExternalUrl() { return externalUrl; }
    public Instant getLastActivityAt() { return lastActivityAt; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public Instant getArchivedAt() { return archivedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
