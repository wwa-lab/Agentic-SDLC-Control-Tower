CREATE TABLE pull_request_review (
    id VARCHAR(64) PRIMARY KEY,
    pr_id VARCHAR(64) NOT NULL,
    reviewer_id VARCHAR(64),
    reviewer_name VARCHAR(255),
    state VARCHAR(32),
    body_summary VARCHAR(4096),
    submitted_at TIMESTAMP,
    CONSTRAINT fk_pull_request_review_pr FOREIGN KEY (pr_id) REFERENCES pull_request (id)
);

CREATE INDEX idx_pull_request_review_pr ON pull_request_review(pr_id);
