package com.sdlctower.domain.testingmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "test_case_result")
public class TestCaseResultEntity {

    @Id
    private String id;

    @Column(name = "run_id", nullable = false)
    private String runId;

    @Column(name = "case_id", nullable = false)
    private String caseId;

    @Column(nullable = false)
    private String outcome;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "last_passed_at")
    private Instant lastPassedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TestCaseResultEntity() {}

    public String getId() { return id; }
    public String getRunId() { return runId; }
    public String getCaseId() { return caseId; }
    public String getOutcome() { return outcome; }
    public Integer getDurationSec() { return durationSec; }
    public Instant getLastPassedAt() { return lastPassedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
