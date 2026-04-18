CREATE TABLE dp_ai_release_notes (
    id VARCHAR(64) PRIMARY KEY,
    release_id VARCHAR(64) NOT NULL,
    skill_version VARCHAR(32),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    body_markdown CLOB,
    diff_narrative CLOB,
    risk_hint VARCHAR(16),
    evidence_hash VARCHAR(128),
    generated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dp_ainotes UNIQUE (release_id, skill_version),
    CONSTRAINT fk_dpainotes_rel FOREIGN KEY (release_id) REFERENCES dp_release(id)
);

CREATE TABLE dp_ai_deployment_summary (
    id VARCHAR(64) PRIMARY KEY,
    deploy_id VARCHAR(64) NOT NULL,
    skill_version VARCHAR(32),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    narrative CLOB,
    evidence_hash VARCHAR(128),
    generated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dp_aisummary UNIQUE (deploy_id, skill_version),
    CONSTRAINT fk_dpaisummary_dep FOREIGN KEY (deploy_id) REFERENCES dp_deploy(id)
);
