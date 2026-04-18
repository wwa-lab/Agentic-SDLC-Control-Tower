package com.sdlctower.domain.reportcenter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "report_fact_wip")
public class WipFactEntity {

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

    @Column(name = "owner_id")
    private String ownerId;

    @Column(nullable = false)
    private String stage;

    @Column(name = "age_bucket", nullable = false)
    private String ageBucket;

    @Column(name = "item_count", nullable = false)
    private int itemCount;

    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;

    protected WipFactEntity() {}

    public Long getId() { return id; }
    public String getOrgId() { return orgId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getTeamId() { return teamId; }
    public String getOwnerId() { return ownerId; }
    public String getStage() { return stage; }
    public String getAgeBucket() { return ageBucket; }
    public int getItemCount() { return itemCount; }
    public Instant getSnapshotAt() { return snapshotAt; }
}
