package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "pipeline_job")
public class PipelineJobEntity {

    @Id
    private String id;

    @Column(name = "run_id", nullable = false)
    private String runId;

    @Column(nullable = false)
    private String name;

    private String status;

    private String conclusion;

    @Column(name = "job_number")
    private int jobNumber;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected PipelineJobEntity() {}

    public String getId() { return id; }
    public String getRunId() { return runId; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getConclusion() { return conclusion; }
    public int getJobNumber() { return jobNumber; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
