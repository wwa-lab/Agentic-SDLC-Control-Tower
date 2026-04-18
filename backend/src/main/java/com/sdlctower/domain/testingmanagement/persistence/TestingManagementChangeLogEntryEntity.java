package com.sdlctower.domain.testingmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "testing_management_change_log")
public class TestingManagementChangeLogEntryEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "entry_type", nullable = false)
    private String entryType;

    @Column(name = "reference_id", nullable = false)
    private String referenceId;

    @Column(name = "actor_member_id")
    private String actorMemberId;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected TestingManagementChangeLogEntryEntity() {}

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getEntryType() { return entryType; }
    public String getReferenceId() { return referenceId; }
    public String getActorMemberId() { return actorMemberId; }
    public String getPayloadJson() { return payloadJson; }
    public Instant getOccurredAt() { return occurredAt; }
}
