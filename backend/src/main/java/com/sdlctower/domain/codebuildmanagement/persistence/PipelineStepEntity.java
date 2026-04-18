package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "pipeline_step")
public class PipelineStepEntity {

    @Id
    private String id;

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @Column(nullable = false)
    private String name;

    @Column(name = "order_index")
    private int orderIndex;

    private String conclusion;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected PipelineStepEntity() {}

    public String getId() { return id; }
    public String getJobId() { return jobId; }
    public String getName() { return name; }
    public int getOrderIndex() { return orderIndex; }
    public String getConclusion() { return conclusion; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
