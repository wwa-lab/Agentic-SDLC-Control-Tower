package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_backfill_checkpoint")
public class JenkinsBackfillCheckpointEntity {

    @Id private String id;
    @Column(name = "jenkins_instance_id", nullable = false) private String jenkinsInstanceId;
    @Column(name = "last_build_timestamp") private Instant lastBuildTimestamp;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected JenkinsBackfillCheckpointEntity() {}

    public String getId() { return id; }
    public String getJenkinsInstanceId() { return jenkinsInstanceId; }
    public Instant getLastBuildTimestamp() { return lastBuildTimestamp; }
    public void setLastBuildTimestamp(Instant v) { this.lastBuildTimestamp = v; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}
