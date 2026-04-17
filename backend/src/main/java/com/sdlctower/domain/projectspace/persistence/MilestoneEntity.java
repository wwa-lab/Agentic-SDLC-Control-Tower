package com.sdlctower.domain.projectspace.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "milestones")
public class MilestoneEntity {

    @Id
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(nullable = false)
    private String label;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "percent_complete")
    private Integer percentComplete;

    @Column(name = "owner_member_id")
    private String ownerMemberId;

    @Column(name = "slippage_reason")
    private String slippageReason;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent;

    @Column(nullable = false)
    private int ordering;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MilestoneEntity() {}

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getLabel() { return label; }
    public LocalDate getTargetDate() { return targetDate; }
    public String getStatus() { return status; }
    public Integer getPercentComplete() { return percentComplete; }
    public String getOwnerMemberId() { return ownerMemberId; }
    public String getSlippageReason() { return slippageReason; }
    public boolean isCurrent() { return isCurrent; }
    public int getOrdering() { return ordering; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
