CREATE TABLE PLATFORM_CONFIGURATION (
    id VARCHAR(64) PRIMARY KEY,
    config_key VARCHAR(256) NOT NULL,
    kind VARCHAR(64) NOT NULL,
    scope_type VARCHAR(32) NOT NULL,
    scope_id VARCHAR(128) NOT NULL,
    parent_id VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    body CLOB,
    has_drift BOOLEAN DEFAULT FALSE NOT NULL,
    last_modified_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX ux_platform_configuration_scope
    ON PLATFORM_CONFIGURATION(config_key, scope_type, scope_id);
