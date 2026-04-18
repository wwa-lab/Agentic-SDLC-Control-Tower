package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_ai_deployment_summary")
public class AiDeploymentSummaryEntity {

    @Id private String id;
    @Column(name = "deploy_id", nullable = false) private String deployId;
    @Column(name = "skill_version") private String skillVersion;
    @Column(nullable = false) private String status;
    @Lob @Basic(fetch = FetchType.LAZY)
    @Column private String narrative;
    @Column(name = "evidence_hash") private String evidenceHash;
    @Column(name = "generated_at") private Instant generatedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AiDeploymentSummaryEntity() {}

    public static AiDeploymentSummaryEntity createPending(String id, String deployId, String skillVersion) {
        var e = new AiDeploymentSummaryEntity();
        e.id = id; e.deployId = deployId; e.skillVersion = skillVersion;
        e.status = "PENDING"; e.createdAt = Instant.now();
        return e;
    }

    public String getId() { return id; }
    public String getDeployId() { return deployId; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getNarrative() { return narrative; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant v) { this.generatedAt = v; }
}
