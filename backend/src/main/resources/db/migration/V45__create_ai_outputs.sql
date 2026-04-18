CREATE TABLE ai_pr_review (
    id VARCHAR(64) PRIMARY KEY,
    pr_id VARCHAR(64) NOT NULL,
    head_sha VARCHAR(40),
    skill_version VARCHAR(64),
    status VARCHAR(16),
    generated_at TIMESTAMP,
    notes_json CLOB,
    error_json VARCHAR(4096),
    invalidated_at TIMESTAMP,
    CONSTRAINT fk_ai_pr_review_pr FOREIGN KEY (pr_id) REFERENCES pull_request (id),
    CONSTRAINT uq_ai_pr_review UNIQUE (pr_id, head_sha, skill_version)
);

CREATE INDEX idx_ai_pr_review_skill_date ON ai_pr_review(skill_version, generated_at DESC);

CREATE TABLE ai_pr_review_note (
    id VARCHAR(64) PRIMARY KEY,
    review_id VARCHAR(64) NOT NULL,
    severity VARCHAR(16),
    file_path VARCHAR(512),
    start_line INT,
    end_line INT,
    message VARCHAR(4096),
    CONSTRAINT fk_ai_pr_review_note_review FOREIGN KEY (review_id) REFERENCES ai_pr_review (id)
);

CREATE INDEX idx_ai_pr_review_note_review ON ai_pr_review_note(review_id);

CREATE TABLE ai_triage_row (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64),
    step_id VARCHAR(64),
    skill_version VARCHAR(64),
    attempt_number INT DEFAULT 1,
    status VARCHAR(16),
    likely_cause VARCHAR(4096),
    candidate_owners_json VARCHAR(2048),
    confidence DOUBLE,
    evidence_json CLOB,
    error_json VARCHAR(4096),
    generated_at TIMESTAMP,
    CONSTRAINT uq_ai_triage_row UNIQUE (run_id, step_id, skill_version)
);

CREATE INDEX idx_ai_triage_row_run ON ai_triage_row(run_id);

CREATE TABLE ai_workspace_summary (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64),
    repo_id VARCHAR(64),
    skill_version VARCHAR(64),
    status VARCHAR(16),
    generated_at TIMESTAMP,
    narrative CLOB,
    evidence_json CLOB,
    CONSTRAINT fk_ai_workspace_summary_repo FOREIGN KEY (repo_id) REFERENCES repo (id),
    CONSTRAINT uq_ai_workspace_summary UNIQUE (workspace_id, repo_id, skill_version)
);

CREATE INDEX idx_ai_workspace_summary_ws ON ai_workspace_summary(workspace_id);
