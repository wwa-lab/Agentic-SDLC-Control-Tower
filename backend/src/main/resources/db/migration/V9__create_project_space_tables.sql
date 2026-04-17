CREATE TABLE milestones (
    id VARCHAR(64) PRIMARY KEY,
    project_id VARCHAR(64) NOT NULL,
    label VARCHAR(256) NOT NULL,
    target_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    percent_complete INT,
    owner_member_id VARCHAR(64),
    slippage_reason VARCHAR(512),
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    ordering INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_milestones_project_ordering
    ON milestones (project_id, ordering);

CREATE TABLE project_dependencies (
    id VARCHAR(64) PRIMARY KEY,
    source_project_id VARCHAR(64) NOT NULL,
    target_name VARCHAR(256) NOT NULL,
    target_ref VARCHAR(256) NOT NULL,
    target_project_id VARCHAR(64),
    direction VARCHAR(16) NOT NULL,
    relationship VARCHAR(16) NOT NULL,
    owner_team VARCHAR(128) NOT NULL,
    health VARCHAR(16) NOT NULL,
    blocker_reason VARCHAR(512),
    external BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_project_deps_source_dir
    ON project_dependencies (source_project_id, direction);

CREATE TABLE environments (
    id VARCHAR(64) PRIMARY KEY,
    project_id VARCHAR(64) NOT NULL,
    label VARCHAR(128) NOT NULL,
    kind VARCHAR(16) NOT NULL,
    gate_status VARCHAR(32) NOT NULL,
    approver_member_id VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_environments_project
    ON environments (project_id);

CREATE TABLE deployments (
    id VARCHAR(64) PRIMARY KEY,
    environment_id VARCHAR(64) NOT NULL,
    version_ref VARCHAR(128) NOT NULL,
    build_id VARCHAR(128) NOT NULL,
    health VARCHAR(16) NOT NULL,
    deployed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    commit_distance_from_prod INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_deployments_env_deployed
    ON deployments (environment_id, deployed_at);
