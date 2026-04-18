CREATE TABLE test_plan (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64) NOT NULL,
    project_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description CLOB,
    owner_member_id VARCHAR(64) NOT NULL,
    state VARCHAR(32) NOT NULL,
    release_target VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE test_case (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64) NOT NULL,
    project_id VARCHAR(64) NOT NULL,
    plan_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    priority VARCHAR(32) NOT NULL,
    state VARCHAR(32) NOT NULL,
    origin VARCHAR(32) NOT NULL,
    owner_member_id VARCHAR(64) NOT NULL,
    preconditions CLOB,
    steps CLOB NOT NULL,
    expected_result CLOB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_test_case_plan FOREIGN KEY (plan_id) REFERENCES test_plan (id)
);

CREATE TABLE test_environment (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64) NOT NULL,
    project_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description CLOB,
    kind VARCHAR(32) NOT NULL,
    url VARCHAR(512),
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_test_plan_workspace_project ON test_plan (workspace_id, project_id);
CREATE INDEX idx_test_case_plan_state ON test_case (plan_id, state);
CREATE INDEX idx_test_environment_workspace ON test_environment (workspace_id, project_id);
