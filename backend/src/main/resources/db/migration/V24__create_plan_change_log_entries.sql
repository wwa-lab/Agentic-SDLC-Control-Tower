CREATE TABLE plan_change_log_entries (
    id VARCHAR(64) PRIMARY KEY,
    project_id VARCHAR(64) NOT NULL,
    actor_type VARCHAR(16) NOT NULL,
    actor_member_id VARCHAR(64),
    skill_execution_id VARCHAR(128),
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    before_json CLOB,
    after_json CLOB,
    reason VARCHAR(512),
    correlation_id VARCHAR(128) NOT NULL,
    audit_link_id VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_plan_change_log_project_created
    ON plan_change_log_entries (project_id, created_at DESC);
