package com.sdlctower.domain.testingmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "test_failure_summary")
public class TestFailureSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "result_id", nullable = false, unique = true)
    private String resultId;

    @Lob
    @Column(name = "failure_excerpt")
    private String failureExcerpt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TestFailureSummaryEntity() {}

    public Long getId() { return id; }
    public String getResultId() { return resultId; }
    public String getFailureExcerpt() { return failureExcerpt; }
    public Instant getCreatedAt() { return createdAt; }
}
