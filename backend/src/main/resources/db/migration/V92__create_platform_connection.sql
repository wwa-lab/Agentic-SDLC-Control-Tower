CREATE TABLE PLATFORM_CREDENTIAL_REF (
    id VARCHAR(128) PRIMARY KEY,
    provider VARCHAR(64) NOT NULL,
    external_ref VARCHAR(512),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE PLATFORM_CONNECTION (
    id VARCHAR(64) PRIMARY KEY,
    kind VARCHAR(64) NOT NULL,
    scope_workspace_id VARCHAR(64) NOT NULL,
    application_id VARCHAR(64),
    application_name VARCHAR(256),
    snow_group_id VARCHAR(64),
    snow_group_name VARCHAR(256),
    base_url VARCHAR(1024),
    credential_ref VARCHAR(128) NOT NULL,
    sync_mode VARCHAR(32) NOT NULL,
    pull_schedule VARCHAR(128),
    push_url VARCHAR(1024),
    status VARCHAR(32) NOT NULL,
    last_sync_at TIMESTAMP,
    last_test_at TIMESTAMP,
    last_test_ok BOOLEAN
);
