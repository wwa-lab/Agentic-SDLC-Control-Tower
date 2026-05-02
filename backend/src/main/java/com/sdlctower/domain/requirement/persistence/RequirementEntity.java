package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.Filter;
import com.sdlctower.shared.persistence.WorkspacePrePersistListener;

@Entity
@Table(name = "requirement")
@Filter(name = "workspace_filter", condition = "workspace_id = :workspaceId")
@EntityListeners(WorkspacePrePersistListener.class)
public class RequirementEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "CLOB")
    private String summary;

    @Column(name = "business_justification", columnDefinition = "CLOB")
    private String businessJustification;

    @Column(nullable = false)
    private String priority;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String source;

    private String assignee;

    @Column(name = "completeness_score", nullable = false)
    private int completenessScore;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    protected RequirementEntity() {}

    public static RequirementEntity create(
            String id,
            String title,
            String summary,
            String businessJustification,
            String priority,
            String status,
            String category,
            String source,
            String assignee,
            int completenessScore,
            Instant createdAt,
            Instant updatedAt,
            String workspaceId
    ) {
        RequirementEntity entity = new RequirementEntity();
        entity.id = id;
        entity.title = title;
        entity.summary = summary;
        entity.businessJustification = businessJustification;
        entity.priority = priority;
        entity.status = status;
        entity.category = category;
        entity.source = source;
        entity.assignee = assignee;
        entity.completenessScore = completenessScore;
        entity.createdAt = createdAt;
        entity.updatedAt = updatedAt;
        entity.workspaceId = workspaceId;
        return entity;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getBusinessJustification() { return businessJustification; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getSource() { return source; }
    public String getAssignee() { return assignee; }
    public int getCompletenessScore() { return completenessScore; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getWorkspaceId() { return workspaceId; }
}
