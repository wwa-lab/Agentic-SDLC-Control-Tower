CREATE TABLE requirement_agent_run (
    execution_id VARCHAR(64) PRIMARY KEY,
    requirement_id VARCHAR(20) NOT NULL,
    profile_id VARCHAR(64) NOT NULL,
    skill_key VARCHAR(128) NOT NULL,
    target_stage VARCHAR(64),
    status VARCHAR(64) NOT NULL,
    manifest CLOB NOT NULL,
    output_summary CLOB,
    error_message CLOB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_req_agent_run_requirement FOREIGN KEY (requirement_id) REFERENCES requirement (id)
);

CREATE TABLE requirement_artifact_link (
    id VARCHAR(64) PRIMARY KEY,
    execution_id VARCHAR(64) NOT NULL,
    requirement_id VARCHAR(20) NOT NULL,
    artifact_type VARCHAR(64) NOT NULL,
    storage_type VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    uri VARCHAR(1024) NOT NULL,
    repo_full_name VARCHAR(255),
    path VARCHAR(512),
    commit_sha VARCHAR(128),
    blob_sha VARCHAR(128),
    status VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_req_artifact_run FOREIGN KEY (execution_id) REFERENCES requirement_agent_run (execution_id),
    CONSTRAINT fk_req_artifact_requirement FOREIGN KEY (requirement_id) REFERENCES requirement (id)
);
