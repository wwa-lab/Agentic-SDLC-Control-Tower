package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_approval_event")
public class ApprovalEventEntity {

    @Id private String id;
    @Column(name = "deploy_id", nullable = false) private String deployId;
    @Column(name = "stage_id") private String stageId;
    @Column(name = "stage_name") private String stageName;
    @Column(name = "approver_member_id") private String approverMemberId;
    @Column(name = "approver_display_name") private String approverDisplayName;
    @Column(name = "approver_role") private String approverRole;
    @Column(nullable = false) private String decision;
    @Column(name = "gate_policy_version") private String gatePolicyVersion;
    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "rationale_cipher") private String rationaleCipher;
    @Column(name = "prompted_at") private Instant promptedAt;
    @Column(name = "decided_at", nullable = false) private Instant decidedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected ApprovalEventEntity() {}

    public String getId() { return id; }
    public String getDeployId() { return deployId; }
    public String getStageId() { return stageId; }
    public String getStageName() { return stageName; }
    public String getApproverMemberId() { return approverMemberId; }
    public String getApproverDisplayName() { return approverDisplayName; }
    public String getApproverRole() { return approverRole; }
    public String getDecision() { return decision; }
    public String getGatePolicyVersion() { return gatePolicyVersion; }
    public String getRationaleCipher() { return rationaleCipher; }
    public Instant getDecidedAt() { return decidedAt; }
}
