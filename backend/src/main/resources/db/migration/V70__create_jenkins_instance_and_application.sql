CREATE TABLE jenkins_instance (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    base_url VARCHAR(512) NOT NULL,
    webhook_secret_encrypted VARCHAR(512),
    api_token_encrypted VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dp_application (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64) NOT NULL,
    project_id VARCHAR(64) NOT NULL,
    jenkins_instance_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(128) NOT NULL,
    runtime_label VARCHAR(32),
    jenkins_folder_path VARCHAR(512),
    description VARCHAR(1024),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dp_app_slug UNIQUE (workspace_id, slug),
    CONSTRAINT fk_dpapp_jenkins FOREIGN KEY (jenkins_instance_id) REFERENCES jenkins_instance(id)
);
