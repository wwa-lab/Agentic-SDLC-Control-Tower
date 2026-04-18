package com.sdlctower.domain.aicenter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "policy", indexes = {
        @Index(name = "idx_policy_ws_skill", columnList = "workspace_id, skill_id", unique = true)
})
public class PolicyEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "skill_id", nullable = false)
    private String skillId;

    @Column(name = "autonomy_level", nullable = false)
    private String autonomyLevel;

    @Column(name = "approval_required_actions", columnDefinition = "CLOB")
    private String approvalRequiredActionsJson;

    @Column(name = "authorized_approver_roles", columnDefinition = "CLOB")
    private String authorizedApproverRolesJson;

    @Column(name = "risk_thresholds", columnDefinition = "CLOB")
    private String riskThresholdsJson;

    @Column(name = "last_changed_at", nullable = false)
    private Instant lastChangedAt;

    @Column(name = "last_changed_by", nullable = false)
    private String lastChangedBy;

    protected PolicyEntity() {}

    public static PolicyEntity create(String id, String workspaceId, String skillId,
                                      String autonomyLevel, String approvalRequiredActionsJson,
                                      String authorizedApproverRolesJson, String riskThresholdsJson,
                                      Instant lastChangedAt, String lastChangedBy) {
        PolicyEntity e = new PolicyEntity();
        e.id = id;
        e.workspaceId = workspaceId;
        e.skillId = skillId;
        e.autonomyLevel = autonomyLevel;
        e.approvalRequiredActionsJson = approvalRequiredActionsJson;
        e.authorizedApproverRolesJson = authorizedApproverRolesJson;
        e.riskThresholdsJson = riskThresholdsJson;
        e.lastChangedAt = lastChangedAt;
        e.lastChangedBy = lastChangedBy;
        return e;
    }

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getSkillId() { return skillId; }
    public String getAutonomyLevel() { return autonomyLevel; }
    public String getApprovalRequiredActionsJson() { return approvalRequiredActionsJson; }
    public String getAuthorizedApproverRolesJson() { return authorizedApproverRolesJson; }
    public String getRiskThresholdsJson() { return riskThresholdsJson; }
    public Instant getLastChangedAt() { return lastChangedAt; }
    public String getLastChangedBy() { return lastChangedBy; }
}
