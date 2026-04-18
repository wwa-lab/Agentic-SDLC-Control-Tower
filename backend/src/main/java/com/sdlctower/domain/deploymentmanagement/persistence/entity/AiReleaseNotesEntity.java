package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_ai_release_notes")
public class AiReleaseNotesEntity {

    @Id private String id;
    @Column(name = "release_id", nullable = false) private String releaseId;
    @Column(name = "skill_version") private String skillVersion;
    @Column(nullable = false) private String status;
    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "body_markdown") private String bodyMarkdown;
    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "diff_narrative") private String diffNarrative;
    @Column(name = "risk_hint") private String riskHint;
    @Column(name = "evidence_hash") private String evidenceHash;
    @Column(name = "generated_at") private Instant generatedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AiReleaseNotesEntity() {}

    public static AiReleaseNotesEntity createPending(String id, String releaseId, String skillVersion) {
        var e = new AiReleaseNotesEntity();
        e.id = id; e.releaseId = releaseId; e.skillVersion = skillVersion;
        e.status = "PENDING"; e.createdAt = Instant.now();
        return e;
    }

    public String getId() { return id; }
    public String getReleaseId() { return releaseId; }
    public String getSkillVersion() { return skillVersion; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getBodyMarkdown() { return bodyMarkdown; }
    public void setBodyMarkdown(String v) { this.bodyMarkdown = v; }
    public String getRiskHint() { return riskHint; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant v) { this.generatedAt = v; }
}
