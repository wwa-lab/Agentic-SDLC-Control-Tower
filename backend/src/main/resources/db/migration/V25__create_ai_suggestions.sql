CREATE TABLE ai_suggestions (
    id VARCHAR(64) PRIMARY KEY,
    project_id VARCHAR(64) NOT NULL,
    kind VARCHAR(32) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    payload_json CLOB NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    state VARCHAR(32) NOT NULL,
    skill_execution_id VARCHAR(128),
    suppress_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_ai_suggestions_project_state
    ON ai_suggestions (project_id, state);
