package com.sdlctower.domain.testingmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "test_run")
public class TestRunEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "plan_id", nullable = false)
    private String planId;

    @Column(name = "environment_id", nullable = false)
    private String environmentId;

    @Column(nullable = false)
    private String state;

    @Column(name = "trigger_source", nullable = false)
    private String triggerSource;

    @Column(name = "actor_member_id", nullable = false)
    private String actorMemberId;

    @Column(name = "external_run_id")
    private String externalRunId;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "pass_count", nullable = false)
    private int passCount;

    @Column(name = "fail_count", nullable = false)
    private int failCount;

    @Column(name = "skip_count", nullable = false)
    private int skipCount;

    @Column(name = "error_count", nullable = false)
    private int errorCount;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected TestRunEntity() {}

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getPlanId() { return planId; }
    public String getEnvironmentId() { return environmentId; }
    public String getState() { return state; }
    public String getTriggerSource() { return triggerSource; }
    public String getActorMemberId() { return actorMemberId; }
    public String getExternalRunId() { return externalRunId; }
    public Integer getDurationSec() { return durationSec; }
    public int getPassCount() { return passCount; }
    public int getFailCount() { return failCount; }
    public int getSkipCount() { return skipCount; }
    public int getErrorCount() { return errorCount; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
