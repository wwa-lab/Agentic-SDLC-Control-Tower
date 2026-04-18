package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "pull_request_review")
public class PullRequestReviewEntity {

    @Id
    private String id;

    @Column(name = "pr_id", nullable = false)
    private String prId;

    @Column(name = "reviewer_id")
    private String reviewerId;

    @Column(name = "reviewer_name")
    private String reviewerName;

    private String state;

    @Column(name = "body_summary", length = 4096)
    private String bodySummary;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    protected PullRequestReviewEntity() {}

    public String getId() { return id; }
    public String getPrId() { return prId; }
    public String getReviewerId() { return reviewerId; }
    public String getReviewerName() { return reviewerName; }
    public String getState() { return state; }
    public String getBodySummary() { return bodySummary; }
    public Instant getSubmittedAt() { return submittedAt; }
}
