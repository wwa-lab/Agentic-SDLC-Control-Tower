package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "requirement_document_quality_gate_run")
public class RequirementDocumentQualityGateRunEntity {
    @Id
    @Column(name = "execution_id")
    private String executionId;
    @Column(name = "document_id", nullable = false)
    private String documentId;
    @Column(name = "requirement_id", nullable = false)
    private String requirementId;
    @Column(name = "profile_id", nullable = false)
    private String profileId;
    @Column(name = "sdd_type", nullable = false)
    private String sddType;
    @Column(nullable = false)
    private int score;
    @Column(nullable = false)
    private String band;
    @Column(nullable = false)
    private boolean passed;
    @Column(nullable = false)
    private int threshold;
    @Column(name = "rubric_version", nullable = false)
    private String rubricVersion;
    @Column(name = "commit_sha", nullable = false)
    private String commitSha;
    @Column(name = "blob_sha", nullable = false)
    private String blobSha;
    @Column(columnDefinition = "CLOB", nullable = false)
    private String dimensions;
    @Column(columnDefinition = "CLOB", nullable = false)
    private String findings;
    @Column(columnDefinition = "CLOB", nullable = false)
    private String summary;
    @Column(name = "triggered_by", nullable = false)
    private String triggeredBy;
    @Column(name = "trigger_mode", nullable = false)
    private String triggerMode;
    @Column(name = "scored_at", nullable = false)
    private Instant scoredAt;

    protected RequirementDocumentQualityGateRunEntity() {}

    public static RequirementDocumentQualityGateRunEntity create(
            String executionId,
            String documentId,
            String requirementId,
            String profileId,
            String sddType,
            int score,
            String band,
            boolean passed,
            int threshold,
            String rubricVersion,
            String commitSha,
            String blobSha,
            String dimensions,
            String findings,
            String summary,
            String triggeredBy,
            String triggerMode,
            Instant scoredAt
    ) {
        RequirementDocumentQualityGateRunEntity entity = new RequirementDocumentQualityGateRunEntity();
        entity.executionId = executionId;
        entity.documentId = documentId;
        entity.requirementId = requirementId;
        entity.profileId = profileId;
        entity.sddType = sddType;
        entity.score = score;
        entity.band = band;
        entity.passed = passed;
        entity.threshold = threshold;
        entity.rubricVersion = rubricVersion;
        entity.commitSha = commitSha;
        entity.blobSha = blobSha;
        entity.dimensions = dimensions;
        entity.findings = findings;
        entity.summary = summary;
        entity.triggeredBy = triggeredBy;
        entity.triggerMode = triggerMode;
        entity.scoredAt = scoredAt;
        return entity;
    }

    public String getExecutionId() { return executionId; }
    public String getDocumentId() { return documentId; }
    public String getRequirementId() { return requirementId; }
    public String getProfileId() { return profileId; }
    public String getSddType() { return sddType; }
    public int getScore() { return score; }
    public String getBand() { return band; }
    public boolean isPassed() { return passed; }
    public int getThreshold() { return threshold; }
    public String getRubricVersion() { return rubricVersion; }
    public String getCommitSha() { return commitSha; }
    public String getBlobSha() { return blobSha; }
    public String getDimensions() { return dimensions; }
    public String getFindings() { return findings; }
    public String getSummary() { return summary; }
    public String getTriggeredBy() { return triggeredBy; }
    public String getTriggerMode() { return triggerMode; }
    public Instant getScoredAt() { return scoredAt; }
}
