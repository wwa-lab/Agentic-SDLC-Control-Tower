package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_deploy")
public class DeployEntity {

    @Id private String id;
    @Column(name = "application_id", nullable = false) private String applicationId;
    @Column(name = "release_id") private String releaseId;
    @Column(name = "environment_name", nullable = false) private String environmentName;
    @Column(name = "jenkins_instance_id") private String jenkinsInstanceId;
    @Column(name = "jenkins_job_full_name") private String jenkinsJobFullName;
    @Column(name = "jenkins_build_number") private Integer jenkinsBuildNumber;
    @Column(name = "jenkins_build_url") private String jenkinsBuildUrl;
    @Column(name = "trigger", nullable = false) private String trigger;
    @Column private String actor;
    @Column(nullable = false) private String state;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "duration_sec") private Long durationSec;
    @Column(name = "is_rollback", nullable = false) private boolean isRollback;
    @Column(name = "rollback_detection_signal") private String rollbackDetectionSignal;
    @Column(name = "hidden_at") private Instant hiddenAt;
    @Column(name = "last_ingested_at") private Instant lastIngestedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected DeployEntity() {}

    public static DeployEntity create(String id, String applicationId, String environmentName,
                                      String trigger, String state, Instant startedAt) {
        var e = new DeployEntity();
        e.id = id; e.applicationId = applicationId; e.environmentName = environmentName;
        e.trigger = trigger; e.state = state; e.startedAt = startedAt;
        e.createdAt = Instant.now(); e.isRollback = false;
        return e;
    }

    public String getId() { return id; }
    public String getApplicationId() { return applicationId; }
    public String getReleaseId() { return releaseId; }
    public void setReleaseId(String releaseId) { this.releaseId = releaseId; }
    public String getEnvironmentName() { return environmentName; }
    public String getJenkinsInstanceId() { return jenkinsInstanceId; }
    public void setJenkinsInstanceId(String v) { this.jenkinsInstanceId = v; }
    public String getJenkinsJobFullName() { return jenkinsJobFullName; }
    public void setJenkinsJobFullName(String v) { this.jenkinsJobFullName = v; }
    public Integer getJenkinsBuildNumber() { return jenkinsBuildNumber; }
    public void setJenkinsBuildNumber(Integer v) { this.jenkinsBuildNumber = v; }
    public String getJenkinsBuildUrl() { return jenkinsBuildUrl; }
    public void setJenkinsBuildUrl(String v) { this.jenkinsBuildUrl = v; }
    public String getTrigger() { return trigger; }
    public String getActor() { return actor; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant v) { this.completedAt = v; }
    public Long getDurationSec() { return durationSec; }
    public void setDurationSec(Long v) { this.durationSec = v; }
    public boolean isRollback() { return isRollback; }
    public void setRollback(boolean v) { this.isRollback = v; }
    public String getRollbackDetectionSignal() { return rollbackDetectionSignal; }
    public void setRollbackDetectionSignal(String v) { this.rollbackDetectionSignal = v; }
    public void setHiddenAt(Instant v) { this.hiddenAt = v; }
    public Instant getLastIngestedAt() { return lastIngestedAt; }
    public void setLastIngestedAt(Instant v) { this.lastIngestedAt = v; }
    public Instant getCreatedAt() { return createdAt; }
}
