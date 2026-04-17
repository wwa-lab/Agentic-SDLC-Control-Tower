package com.sdlctower.domain.projectspace.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "project_dependencies")
public class ProjectDependencyEntity {

    @Id
    private String id;

    @Column(name = "source_project_id", nullable = false)
    private String sourceProjectId;

    @Column(name = "target_name", nullable = false)
    private String targetName;

    @Column(name = "target_ref", nullable = false)
    private String targetRef;

    @Column(name = "target_project_id")
    private String targetProjectId;

    @Column(nullable = false)
    private String direction;

    @Column(nullable = false)
    private String relationship;

    @Column(name = "owner_team", nullable = false)
    private String ownerTeam;

    @Column(nullable = false)
    private String health;

    @Column(name = "pm_state")
    private String pmState;

    @Column(name = "blocker_reason")
    private String blockerReason;

    @Column(name = "contract_commitment", columnDefinition = "CLOB")
    private String contractCommitment;

    @Column(name = "rejection_reason", columnDefinition = "CLOB")
    private String rejectionReason;

    @Column(name = "counter_signed_by")
    private String counterSignedBy;

    @Column(name = "counter_signed_at")
    private Instant counterSignedAt;

    @Column(name = "plan_revision_at_update")
    private long planRevisionAtUpdate;

    @Column(nullable = false)
    private boolean external;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProjectDependencyEntity() {}

    public static ProjectDependencyEntity create(
            String id,
            String sourceProjectId,
            String targetName,
            String targetRef,
            String targetProjectId,
            String direction,
            String relationship,
            String ownerTeam,
            String health,
            String blockerReason,
            boolean external,
            Instant createdAt,
            Instant updatedAt
    ) {
        ProjectDependencyEntity entity = new ProjectDependencyEntity();
        entity.id = id;
        entity.sourceProjectId = sourceProjectId;
        entity.targetName = targetName;
        entity.targetRef = targetRef;
        entity.targetProjectId = targetProjectId;
        entity.direction = direction;
        entity.relationship = relationship;
        entity.ownerTeam = ownerTeam;
        entity.health = health;
        entity.blockerReason = blockerReason;
        entity.external = external;
        entity.createdAt = createdAt;
        entity.updatedAt = updatedAt;
        return entity;
    }

    public String getId() { return id; }
    public String getSourceProjectId() { return sourceProjectId; }
    public String getTargetName() { return targetName; }
    public String getTargetRef() { return targetRef; }
    public String getTargetProjectId() { return targetProjectId; }
    public String getDirection() { return direction; }
    public String getRelationship() { return relationship; }
    public String getOwnerTeam() { return ownerTeam; }
    public String getHealth() { return health; }
    public String getPmState() { return pmState; }
    public String getBlockerReason() { return blockerReason; }
    public String getContractCommitment() { return contractCommitment; }
    public String getRejectionReason() { return rejectionReason; }
    public String getCounterSignedBy() { return counterSignedBy; }
    public Instant getCounterSignedAt() { return counterSignedAt; }
    public long getPlanRevisionAtUpdate() { return planRevisionAtUpdate; }
    public boolean isExternal() { return external; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setTargetRef(String targetRef) { this.targetRef = targetRef; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public void setTargetProjectId(String targetProjectId) { this.targetProjectId = targetProjectId; }
    public void setOwnerTeam(String ownerTeam) { this.ownerTeam = ownerTeam; }
    public void setHealth(String health) { this.health = health; }
    public void setPmState(String pmState) { this.pmState = pmState; }
    public void setBlockerReason(String blockerReason) { this.blockerReason = blockerReason; }
    public void setContractCommitment(String contractCommitment) { this.contractCommitment = contractCommitment; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setCounterSignedBy(String counterSignedBy) { this.counterSignedBy = counterSignedBy; }
    public void setCounterSignedAt(Instant counterSignedAt) { this.counterSignedAt = counterSignedAt; }
    public void setPlanRevisionAtUpdate(long planRevisionAtUpdate) { this.planRevisionAtUpdate = planRevisionAtUpdate; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
