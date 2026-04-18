CREATE TABLE dp_release (
    id VARCHAR(64) PRIMARY KEY,
    application_id VARCHAR(64) NOT NULL,
    release_version VARCHAR(128) NOT NULL,
    build_artifact_slice_id VARCHAR(32),
    build_artifact_id VARCHAR(64),
    state VARCHAR(16) NOT NULL DEFAULT 'PREPARED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    jenkins_source_url VARCHAR(512),
    CONSTRAINT uq_dp_release_ver UNIQUE (application_id, release_version),
    CONSTRAINT fk_dprel_app FOREIGN KEY (application_id) REFERENCES dp_application(id)
);
CREATE INDEX idx_dprel_artifact ON dp_release(build_artifact_slice_id, build_artifact_id);
CREATE INDEX idx_dprel_state ON dp_release(state);
