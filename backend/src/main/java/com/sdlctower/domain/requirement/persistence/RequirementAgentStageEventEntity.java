package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import com.sdlctower.shared.persistence.WorkspacePrePersistListener;

@Entity
@Table(name = "requirement_agent_stage_event")
@EntityListeners(WorkspacePrePersistListener.class)
public class RequirementAgentStageEventEntity {
    @Id
    private String id;
    @Column(name = "execution_id", nullable = false)
    private String executionId;
    @Column(name = "requirement_id", nullable = false)
    private String requirementId;
    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;
    @Column(name = "profile_id", nullable = false)
    private String profileId;
    @Column(name = "stage_id", nullable = false)
    private String stageId;
    @Column(name = "stage_label")
    private String stageLabel;
    @Column(nullable = false)
    private String state;
    private String message;
    @Column(name = "output_path")
    private String outputPath;
    @Column(name = "error_message", columnDefinition = "CLOB")
    private String errorMessage;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RequirementAgentStageEventEntity() {}

    public static RequirementAgentStageEventEntity create(
            String id,
            String executionId,
            String requirementId,
            String profileId,
            String stageId,
            String stageLabel,
            String state,
            String message,
            String outputPath,
            String errorMessage,
            Instant createdAt
    ) {
        RequirementAgentStageEventEntity entity = new RequirementAgentStageEventEntity();
        entity.id = id;
        entity.executionId = executionId;
        entity.requirementId = requirementId;
        entity.profileId = profileId;
        entity.stageId = stageId;
        entity.stageLabel = stageLabel;
        entity.state = state;
        entity.message = message;
        entity.outputPath = outputPath;
        entity.errorMessage = errorMessage;
        entity.createdAt = createdAt;
        return entity;
    }

    public String getId() { return id; }
    public String getExecutionId() { return executionId; }
    public String getRequirementId() { return requirementId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProfileId() { return profileId; }
    public String getStageId() { return stageId; }
    public String getStageLabel() { return stageLabel; }
    public String getState() { return state; }
    public String getMessage() { return message; }
    public String getOutputPath() { return outputPath; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
}
