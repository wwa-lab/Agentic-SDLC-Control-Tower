CREATE TABLE design_artifact_version (
    id VARCHAR(64) PRIMARY KEY,
    artifact_id VARCHAR(64) NOT NULL,
    version_number INT NOT NULL,
    html_payload CLOB NOT NULL,
    html_size_bytes BIGINT NOT NULL,
    content_sha256 VARCHAR(64) NOT NULL,
    changelog_note CLOB,
    created_by_member_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_design_artifact_version_size CHECK (html_size_bytes <= 2097152),
    CONSTRAINT fk_design_artifact_version_artifact FOREIGN KEY (artifact_id) REFERENCES design_artifact (id)
);

CREATE INDEX idx_design_artifact_version_artifact_number
    ON design_artifact_version (artifact_id, version_number DESC);

ALTER TABLE design_artifact
    ADD CONSTRAINT fk_design_artifact_current_version
    FOREIGN KEY (current_version_id) REFERENCES design_artifact_version (id);
