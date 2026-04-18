CREATE TABLE design_change_log (
    id VARCHAR(64) PRIMARY KEY,
    artifact_id VARCHAR(64) NOT NULL,
    entry_type VARCHAR(64) NOT NULL,
    actor_member_id VARCHAR(64),
    actor_skill_execution_id VARCHAR(128),
    before_json CLOB,
    after_json CLOB,
    reason CLOB,
    correlation_id VARCHAR(128) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_design_change_log_artifact FOREIGN KEY (artifact_id) REFERENCES design_artifact (id)
);

CREATE INDEX idx_design_change_log_artifact_occurred
    ON design_change_log (artifact_id, occurred_at DESC);
