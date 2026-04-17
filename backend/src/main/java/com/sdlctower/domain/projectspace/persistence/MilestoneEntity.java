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

    @Column(columnDefinition = "CLOB")
    private String description;

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

    @Column(name = "pm_state")
    private String pmState;

    @Column(name = "baseline_target_date")
    private LocalDate baselineTargetDate;

    @Column(name = "slippage_risk_score")
    private String slippageRiskScore;

    @Column(name = "slippage_risk_factors", columnDefinition = "CLOB")
    private String slippageRiskFactors;

    @Column(name = "plan_revision_at_update")
    private long planRevisionAtUpdate;

    @Column(name = "ai_suggestion_id")
    private String aiSuggestionId;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent;

    @Column(nullable = false)
    private int ordering;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MilestoneEntity() {}

    public static MilestoneEntity create(
            String id,
            String projectId,
            String label,
            String description,
            LocalDate targetDate,
            String status,
            Integer percentComplete,
            String ownerMemberId,
            String slippageReason,
            boolean isCurrent,
            int ordering,
            Instant createdAt,
            Instant updatedAt
    ) {
        MilestoneEntity entity = new MilestoneEntity();
        entity.id = id;
        entity.projectId = projectId;
        entity.label = label;
        entity.description = description;
        entity.targetDate = targetDate;
        entity.status = status;
        entity.pmState = status;
        entity.percentComplete = percentComplete;
        entity.ownerMemberId = ownerMemberId;
        entity.slippageReason = slippageReason;
        entity.baselineTargetDate = targetDate;
        entity.isCurrent = isCurrent;
        entity.ordering = ordering;
        entity.createdAt = createdAt;
        entity.updatedAt = updatedAt;
        return entity;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public LocalDate getTargetDate() { return targetDate; }
    public String getStatus() { return status; }
    public Integer getPercentComplete() { return percentComplete; }
    public String getOwnerMemberId() { return ownerMemberId; }
    public String getSlippageReason() { return slippageReason; }
    public String getPmState() { return pmState; }
    public LocalDate getBaselineTargetDate() { return baselineTargetDate; }
    public String getSlippageRiskScore() { return slippageRiskScore; }
    public String getSlippageRiskFactors() { return slippageRiskFactors; }
    public long getPlanRevisionAtUpdate() { return planRevisionAtUpdate; }
    public String getAiSuggestionId() { return aiSuggestionId; }
    public boolean isCurrent() { return isCurrent; }
    public int getOrdering() { return ordering; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getArchivedAt() { return archivedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setLabel(String label) { this.label = label; }
    public void setDescription(String description) { this.description = description; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public void setStatus(String status) { this.status = status; }
    public void setPercentComplete(Integer percentComplete) { this.percentComplete = percentComplete; }
    public void setOwnerMemberId(String ownerMemberId) { this.ownerMemberId = ownerMemberId; }
    public void setSlippageReason(String slippageReason) { this.slippageReason = slippageReason; }
    public void setPmState(String pmState) { this.pmState = pmState; }
    public void setBaselineTargetDate(LocalDate baselineTargetDate) { this.baselineTargetDate = baselineTargetDate; }
    public void setSlippageRiskScore(String slippageRiskScore) { this.slippageRiskScore = slippageRiskScore; }
    public void setSlippageRiskFactors(String slippageRiskFactors) { this.slippageRiskFactors = slippageRiskFactors; }
    public void setPlanRevisionAtUpdate(long planRevisionAtUpdate) { this.planRevisionAtUpdate = planRevisionAtUpdate; }
    public void setAiSuggestionId(String aiSuggestionId) { this.aiSuggestionId = aiSuggestionId; }
    public void setCurrent(boolean current) { isCurrent = current; }
    public void setOrdering(int ordering) { this.ordering = ordering; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public void setArchivedAt(Instant archivedAt) { this.archivedAt = archivedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
