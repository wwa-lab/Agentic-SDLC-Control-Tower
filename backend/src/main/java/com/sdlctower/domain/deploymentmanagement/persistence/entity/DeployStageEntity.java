package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_deploy_stage")
public class DeployStageEntity {

    @Id private String id;
    @Column(name = "deploy_id", nullable = false) private String deployId;
    @Column(nullable = false) private String name;
    @Column(name = "stage_order", nullable = false) private int stageOrder;
    @Column(nullable = false) private String state;
    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "duration_sec") private Long durationSec;
    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "log_excerpt_text") private String logExcerptText;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected DeployStageEntity() {}

    public String getId() { return id; }
    public String getDeployId() { return deployId; }
    public String getName() { return name; }
    public int getStageOrder() { return stageOrder; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Long getDurationSec() { return durationSec; }
    public String getLogExcerptText() { return logExcerptText; }
    public void setLogExcerptText(String v) { this.logExcerptText = v; }
}
