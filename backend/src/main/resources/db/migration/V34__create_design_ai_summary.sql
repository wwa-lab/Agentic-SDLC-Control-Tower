CREATE TABLE design_ai_summary (
    id VARCHAR(64) PRIMARY KEY,
    artifact_id VARCHAR(64) NOT NULL,
    version_id VARCHAR(64) NOT NULL,
    skill_version VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL,
    payload_json CLOB,
    error_json CLOB,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_design_ai_summary_artifact FOREIGN KEY (artifact_id) REFERENCES design_artifact (id),
    CONSTRAINT fk_design_ai_summary_version FOREIGN KEY (version_id) REFERENCES design_artifact_version (id)
);

CREATE INDEX idx_design_ai_summary_artifact_version
    ON design_ai_summary (artifact_id, version_id, generated_at DESC);
