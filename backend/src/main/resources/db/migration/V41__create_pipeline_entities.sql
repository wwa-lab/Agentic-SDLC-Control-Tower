CREATE TABLE pipeline_run (
    id VARCHAR(64) PRIMARY KEY,
    repo_id VARCHAR(64) NOT NULL,
    run_number INT,
    pipeline_name VARCHAR(255),
    trigger VARCHAR(32),
    branch VARCHAR(255),
    actor VARCHAR(255),
    head_sha VARCHAR(40),
    status VARCHAR(16),
    duration_sec INT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    external_url VARCHAR(512),
    github_run_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pipeline_run_repo FOREIGN KEY (repo_id) REFERENCES repo (id)
);

CREATE INDEX idx_pipeline_run_repo_created ON pipeline_run(repo_id, created_at DESC);
CREATE INDEX idx_pipeline_run_repo_sha ON pipeline_run(repo_id, head_sha);

CREATE TABLE pipeline_job (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL,
    name VARCHAR(255),
    status VARCHAR(16),
    conclusion VARCHAR(16),
    job_number INT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_pipeline_job_run FOREIGN KEY (run_id) REFERENCES pipeline_run (id)
);

CREATE INDEX idx_pipeline_job_run_number ON pipeline_job(run_id, job_number);

CREATE TABLE pipeline_step (
    id VARCHAR(64) PRIMARY KEY,
    job_id VARCHAR(64) NOT NULL,
    name VARCHAR(255),
    order_index INT,
    conclusion VARCHAR(16),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_pipeline_step_job FOREIGN KEY (job_id) REFERENCES pipeline_job (id)
);

CREATE INDEX idx_pipeline_step_job_order ON pipeline_step(job_id, order_index);

CREATE TABLE check_run (
    id VARCHAR(64) PRIMARY KEY,
    repo_id VARCHAR(64) NOT NULL,
    head_sha VARCHAR(40),
    name VARCHAR(255),
    status VARCHAR(16),
    conclusion VARCHAR(16),
    external_url VARCHAR(512),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_check_run_repo FOREIGN KEY (repo_id) REFERENCES repo (id)
);

CREATE INDEX idx_check_run_repo_sha ON check_run(repo_id, head_sha);
