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
@Table(name = "report_fact_lead_time")
public class LeadTimeFactEntity {

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

    @Column(name = "requirement_type")
    private String requirementType;

    @Column(name = "bucket_date", nullable = false)
    private LocalDate bucketDate;

    @Column(name = "lead_time_minutes", nullable = false)
    private int leadTimeMinutes;

    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;

    protected LeadTimeFactEntity() {}

    public Long getId() { return id; }
    public String getOrgId() { return orgId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getTeamId() { return teamId; }
    public String getRequirementType() { return requirementType; }
    public LocalDate getBucketDate() { return bucketDate; }
    public int getLeadTimeMinutes() { return leadTimeMinutes; }
    public Instant getSnapshotAt() { return snapshotAt; }
}
