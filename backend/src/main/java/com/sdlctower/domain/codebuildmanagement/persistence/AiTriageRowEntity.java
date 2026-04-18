package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ai_triage_row")
public class AiTriageRowEntity {

    @Id
    private String id;

    @Column(name = "run_id", nullable = false)
    private String runId;

    @Column(name = "step_id")
    private String stepId;

    @Column(name = "skill_version")
    private String skillVersion;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber = 1;

    private String status;

    @Column(name = "likely_cause", length = 4096)
    private String likelyCause;

    @Column(name = "candidate_owners_json")
    private String candidateOwnersJson;

    private double confidence;

    @Lob
    @Column(name = "evidence_json")
    private String evidenceJson;

    @Column(name = "error_json")
    private String errorJson;

    @Column(name = "generated_at")
    private Instant generatedAt;

    protected AiTriageRowEntity() {}

    public String getId() { return id; }
    public String getRunId() { return runId; }
    public String getStepId() { return stepId; }
    public String getSkillVersion() { return skillVersion; }
    public int getAttemptNumber() { return attemptNumber; }
    public String getStatus() { return status; }
    public String getLikelyCause() { return likelyCause; }
    public String getCandidateOwnersJson() { return candidateOwnersJson; }
    public double getConfidence() { return confidence; }
    public String getEvidenceJson() { return evidenceJson; }
    public String getErrorJson() { return errorJson; }
    public Instant getGeneratedAt() { return generatedAt; }
}
