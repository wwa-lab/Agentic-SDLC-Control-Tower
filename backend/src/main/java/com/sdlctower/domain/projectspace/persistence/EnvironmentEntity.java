package com.sdlctower.domain.projectspace.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "environments")
public class EnvironmentEntity {

    @Id
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String kind;

    @Column(name = "gate_status", nullable = false)
    private String gateStatus;

    @Column(name = "approver_member_id")
    private String approverMemberId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EnvironmentEntity() {}

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getLabel() { return label; }
    public String getKind() { return kind; }
    public String getGateStatus() { return gateStatus; }
    public String getApproverMemberId() { return approverMemberId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
