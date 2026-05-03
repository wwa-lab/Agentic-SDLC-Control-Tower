CREATE TABLE requirement_agent_stage_event (
    id VARCHAR(64) PRIMARY KEY,
    execution_id VARCHAR(64) NOT NULL,
    requirement_id VARCHAR(20) NOT NULL,
    profile_id VARCHAR(64) NOT NULL,
    stage_id VARCHAR(64) NOT NULL,
    stage_label VARCHAR(128),
    state VARCHAR(64) NOT NULL,
    message VARCHAR(1024),
    output_path VARCHAR(512),
    error_message CLOB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_req_agent_stage_run FOREIGN KEY (execution_id) REFERENCES requirement_agent_run (execution_id),
    CONSTRAINT fk_req_agent_stage_requirement FOREIGN KEY (requirement_id) REFERENCES requirement (id)
);
