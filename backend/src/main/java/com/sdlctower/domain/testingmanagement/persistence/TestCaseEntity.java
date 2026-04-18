package com.sdlctower.domain.testingmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "test_case")
public class TestCaseEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "plan_id", nullable = false)
    private String planId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String priority;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String origin;

    @Column(name = "owner_member_id", nullable = false)
    private String ownerMemberId;

    @Lob
    private String preconditions;

    @Lob
    private String steps;

    @Lob
    @Column(name = "expected_result")
    private String expectedResult;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TestCaseEntity() {}

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getPlanId() { return planId; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getPriority() { return priority; }
    public String getState() { return state; }
    public String getOrigin() { return origin; }
    public String getOwnerMemberId() { return ownerMemberId; }
    public String getPreconditions() { return preconditions; }
    public String getSteps() { return steps; }
    public String getExpectedResult() { return expectedResult; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
