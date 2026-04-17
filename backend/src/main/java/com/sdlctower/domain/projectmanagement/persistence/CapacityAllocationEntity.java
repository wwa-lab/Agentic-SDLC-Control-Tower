package com.sdlctower.domain.projectmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "capacity_allocations")
public class CapacityAllocationEntity {

    @Id
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "milestone_id", nullable = false)
    private String milestoneId;

    @Column(name = "allocation_percent", nullable = false)
    private int allocationPercent;

    @Column
    private String justification;

    @Column(name = "window_start", nullable = false)
    private LocalDate windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDate windowEnd;

    @Column(name = "plan_revision", nullable = false)
    private long planRevision;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CapacityAllocationEntity() {}

    public static CapacityAllocationEntity create(
            String id,
            String projectId,
            String memberId,
            String milestoneId,
            int allocationPercent,
            String justification,
            LocalDate windowStart,
            LocalDate windowEnd,
            long planRevision,
            Instant now
    ) {
        CapacityAllocationEntity entity = new CapacityAllocationEntity();
        entity.id = id;
        entity.projectId = projectId;
        entity.memberId = memberId;
        entity.milestoneId = milestoneId;
        entity.allocationPercent = allocationPercent;
        entity.justification = justification;
        entity.windowStart = windowStart;
        entity.windowEnd = windowEnd;
        entity.planRevision = planRevision;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getMemberId() { return memberId; }
    public String getMilestoneId() { return milestoneId; }
    public int getAllocationPercent() { return allocationPercent; }
    public String getJustification() { return justification; }
    public LocalDate getWindowStart() { return windowStart; }
    public LocalDate getWindowEnd() { return windowEnd; }
    public long getPlanRevision() { return planRevision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void apply(int allocationPercent, String justification, LocalDate windowStart, LocalDate windowEnd, long planRevision, Instant now) {
        this.allocationPercent = allocationPercent;
        this.justification = justification;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.planRevision = planRevision;
        this.updatedAt = now;
    }
}
