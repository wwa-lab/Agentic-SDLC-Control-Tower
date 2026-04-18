package com.sdlctower.domain.reportcenter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "report_fact_cycle_time")
public class CycleTimeFactEntity {

    @Id
    private Long id;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "team_id")
    private String teamId;

    @Column(nullable = false)
    private String stage;

    @Column(name = "bucket_date", nullable = false)
    private LocalDate bucketDate;

    @Column(name = "cycle_time_minutes", nullable = false)
    private int cycleTimeMinutes;

    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;

    protected CycleTimeFactEntity() {}

    public Long getId() { return id; }
    public String getOrgId() { return orgId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getTeamId() { return teamId; }
    public String getStage() { return stage; }
    public LocalDate getBucketDate() { return bucketDate; }
    public int getCycleTimeMinutes() { return cycleTimeMinutes; }
    public Instant getSnapshotAt() { return snapshotAt; }
}
