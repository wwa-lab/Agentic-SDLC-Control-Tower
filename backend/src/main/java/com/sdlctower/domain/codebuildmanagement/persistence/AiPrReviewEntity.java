package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ai_pr_review")
public class AiPrReviewEntity {

    @Id
    private String id;

    @Column(name = "pr_id", nullable = false)
    private String prId;

    @Column(name = "head_sha")
    private String headSha;

    @Column(name = "skill_version")
    private String skillVersion;

    private String status;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @Lob
    @Column(name = "notes_json")
    private String notesJson;

    @Column(name = "error_json")
    private String errorJson;

    @Column(name = "invalidated_at")
    private Instant invalidatedAt;

    protected AiPrReviewEntity() {}

    public String getId() { return id; }
    public String getPrId() { return prId; }
    public String getHeadSha() { return headSha; }
    public String getSkillVersion() { return skillVersion; }
    public String getStatus() { return status; }
    public Instant getGeneratedAt() { return generatedAt; }
    public String getNotesJson() { return notesJson; }
    public String getErrorJson() { return errorJson; }
    public Instant getInvalidatedAt() { return invalidatedAt; }
}
