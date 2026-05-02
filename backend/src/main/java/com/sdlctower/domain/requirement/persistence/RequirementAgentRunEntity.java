package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import com.sdlctower.shared.persistence.WorkspacePrePersistListener;

@Entity
@Table(name = "requirement_agent_run")
@EntityListeners(WorkspacePrePersistListener.class)
public class RequirementAgentRunEntity {
    @Id
    @Column(name = "execution_id")
    private String executionId;
    @Column(name = "requirement_id", nullable = false)
    private String requirementId;
    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;
    @Column(name = "profile_id", nullable = false)
    private String profileId;
    @Column(name = "skill_key", nullable = false)
    private String skillKey;
    @Column(name = "target_stage")
    private String targetStage;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false, columnDefinition = "CLOB")
    private String manifest;
    @Column(name = "output_summary", columnDefinition = "CLOB")
    private String outputSummary;
    @Column(name = "error_message", columnDefinition = "CLOB")
    private String errorMessage;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RequirementAgentRunEntity() {}

    public static RequirementAgentRunEntity create(String executionId, String requirementId, String profileId, String skillKey, String targetStage, String status, String manifest, Instant now) {
        RequirementAgentRunEntity entity = new RequirementAgentRunEntity();
        entity.executionId = executionId;
        entity.requirementId = requirementId;
        entity.profileId = profileId;
        entity.skillKey = skillKey;
        entity.targetStage = targetStage;
        entity.status = status;
        entity.manifest = manifest;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public void applyCallback(String status, String outputSummary, String errorMessage, Instant updatedAt) {
        this.status = status;
        this.outputSummary = outputSummary;
        this.errorMessage = errorMessage;
        this.updatedAt = updatedAt;
    }

    public String getExecutionId() { return executionId; }
    public String getRequirementId() { return requirementId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProfileId() { return profileId; }
    public String getSkillKey() { return skillKey; }
    public String getTargetStage() { return targetStage; }
    public String getStatus() { return status; }
    public String getManifest() { return manifest; }
    public String getOutputSummary() { return outputSummary; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
