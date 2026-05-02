CREATE TABLE PLATFORM_POLICY (
    id VARCHAR(64) PRIMARY KEY,
    policy_key VARCHAR(128) NOT NULL,
    name VARCHAR(256) NOT NULL,
    category VARCHAR(64) NOT NULL,
    scope_type VARCHAR(32) NOT NULL,
    scope_id VARCHAR(128) NOT NULL,
    bound_to VARCHAR(256),
    version_number INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    body CLOB,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(64) NOT NULL
);

CREATE TABLE PLATFORM_POLICY_EXCEPTION (
    id VARCHAR(64) PRIMARY KEY,
    policy_id VARCHAR(64) NOT NULL,
    reason VARCHAR(1024) NOT NULL,
    requester_id VARCHAR(64) NOT NULL,
    approver_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP
);
