package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "pipeline_run")
public class PipelineRunEntity {

    @Id
    private String id;

    @Column(name = "repo_id", nullable = false)
    private String repoId;

    @Column(name = "run_number", nullable = false)
    private int runNumber;

    @Column(name = "pipeline_name")
    private String pipelineName;

    @Column(name = "trigger")
    private String trigger;

    private String branch;

    private String actor;

    @Column(name = "head_sha")
    private String headSha;

    private String status;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "github_run_id")
    private Long githubRunId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PipelineRunEntity() {}

    public String getId() { return id; }
    public String getRepoId() { return repoId; }
    public int getRunNumber() { return runNumber; }
    public String getPipelineName() { return pipelineName; }
    public String getTrigger() { return trigger; }
    public String getBranch() { return branch; }
    public String getActor() { return actor; }
    public String getHeadSha() { return headSha; }
    public String getStatus() { return status; }
    public Integer getDurationSec() { return durationSec; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getExternalUrl() { return externalUrl; }
    public Long getGithubRunId() { return githubRunId; }
    public Instant getCreatedAt() { return createdAt; }
}
