CREATE TABLE dp_deploy (
    id VARCHAR(64) PRIMARY KEY,
    application_id VARCHAR(64) NOT NULL,
    release_id VARCHAR(64),
    environment_name VARCHAR(64) NOT NULL,
    jenkins_instance_id VARCHAR(64),
    jenkins_job_full_name VARCHAR(512),
    jenkins_build_number INT,
    jenkins_build_url VARCHAR(512),
    trigger VARCHAR(32) NOT NULL,
    actor VARCHAR(128),
    state VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration_sec BIGINT,
    is_rollback BOOLEAN NOT NULL DEFAULT FALSE,
    rollback_detection_signal VARCHAR(64),
    hidden_at TIMESTAMP,
    last_ingested_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dp_deploy_jenkins UNIQUE (jenkins_instance_id, jenkins_job_full_name, jenkins_build_number),
    CONSTRAINT fk_dpdeploy_app FOREIGN KEY (application_id) REFERENCES dp_application(id),
    CONSTRAINT fk_dpdeploy_rel FOREIGN KEY (release_id) REFERENCES dp_release(id)
);
CREATE INDEX idx_dpdeploy_app_env ON dp_deploy(application_id, environment_name, completed_at DESC);
CREATE INDEX idx_dpdeploy_release ON dp_deploy(release_id);
CREATE INDEX idx_dpdeploy_state ON dp_deploy(state);
CREATE INDEX idx_dpdeploy_ingested ON dp_deploy(last_ingested_at);

CREATE TABLE dp_deploy_stage (
    id VARCHAR(64) PRIMARY KEY,
    deploy_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    stage_order INT NOT NULL,
    state VARCHAR(16) NOT NULL DEFAULT 'NOT_STARTED',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    duration_sec BIGINT,
    log_excerpt_text CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dpstage_deploy FOREIGN KEY (deploy_id) REFERENCES dp_deploy(id)
);
CREATE INDEX idx_dpstage_deploy ON dp_deploy_stage(deploy_id, stage_order);
