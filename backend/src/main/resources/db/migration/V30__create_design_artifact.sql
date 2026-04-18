CREATE TABLE design_artifact (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64) NOT NULL,
    project_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    format VARCHAR(32) NOT NULL,
    lifecycle VARCHAR(32) NOT NULL,
    current_version_id VARCHAR(64),
    registered_by_member_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_design_artifact_workspace_updated
    ON design_artifact (workspace_id, updated_at DESC);

CREATE INDEX idx_design_artifact_project_lifecycle
    ON design_artifact (project_id, lifecycle);
