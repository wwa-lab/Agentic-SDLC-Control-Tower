package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "check_run")
public class CheckRunEntity {

    @Id
    private String id;

    @Column(name = "repo_id", nullable = false)
    private String repoId;

    @Column(name = "head_sha", nullable = false)
    private String headSha;

    @Column(nullable = false)
    private String name;

    private String status;

    private String conclusion;

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected CheckRunEntity() {}

    public String getId() { return id; }
    public String getRepoId() { return repoId; }
    public String getHeadSha() { return headSha; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getConclusion() { return conclusion; }
    public String getExternalUrl() { return externalUrl; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
