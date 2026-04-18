CREATE TABLE dp_approval_event (
    id VARCHAR(64) PRIMARY KEY,
    deploy_id VARCHAR(64) NOT NULL,
    stage_id VARCHAR(64),
    stage_name VARCHAR(128),
    approver_member_id VARCHAR(64),
    approver_display_name VARCHAR(128),
    approver_role VARCHAR(32),
    decision VARCHAR(16) NOT NULL,
    gate_policy_version VARCHAR(32),
    rationale_cipher CLOB,
    prompted_at TIMESTAMP,
    decided_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dpappr_deploy FOREIGN KEY (deploy_id) REFERENCES dp_deploy(id)
);
CREATE INDEX idx_dpappr_deploy ON dp_approval_event(deploy_id);
