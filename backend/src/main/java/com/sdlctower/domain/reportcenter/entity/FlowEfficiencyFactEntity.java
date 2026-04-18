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
@Table(name = "report_fact_flow_efficiency")
public class FlowEfficiencyFactEntity {

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

    @Column(name = "active_minutes", nullable = false)
    private int activeMinutes;

    @Column(name = "total_minutes", nullable = false)
    private int totalMinutes;

    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;

    protected FlowEfficiencyFactEntity() {}

    public Long getId() { return id; }
    public String getOrgId() { return orgId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getTeamId() { return teamId; }
    public String getStage() { return stage; }
    public LocalDate getBucketDate() { return bucketDate; }
    public int getActiveMinutes() { return activeMinutes; }
    public int getTotalMinutes() { return totalMinutes; }
    public Instant getSnapshotAt() { return snapshotAt; }
}
