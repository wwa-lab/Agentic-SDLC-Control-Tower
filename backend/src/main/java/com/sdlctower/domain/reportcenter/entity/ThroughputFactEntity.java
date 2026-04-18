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
@Table(name = "report_fact_throughput")
public class ThroughputFactEntity {

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

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "items_completed", nullable = false)
    private int itemsCompleted;

    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;

    protected ThroughputFactEntity() {}

    public Long getId() { return id; }
    public String getOrgId() { return orgId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getTeamId() { return teamId; }
    public LocalDate getWeekStart() { return weekStart; }
    public int getItemsCompleted() { return itemsCompleted; }
    public Instant getSnapshotAt() { return snapshotAt; }
}
