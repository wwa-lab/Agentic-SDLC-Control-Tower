CREATE TABLE PLATFORM_APPLICATION (
    id VARCHAR(64) PRIMARY KEY,
    app_key VARCHAR(128) NOT NULL,
    name VARCHAR(256) NOT NULL,
    owner_snow_group_id VARCHAR(64),
    criticality VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE PLATFORM_SNOW_GROUP (
    id VARCHAR(64) PRIMARY KEY,
    servicenow_group_name VARCHAR(256) NOT NULL,
    display_name VARCHAR(256) NOT NULL,
    owner_email VARCHAR(256),
    escalation_policy VARCHAR(256),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE PLATFORM_WORKSPACE (
    id VARCHAR(64) PRIMARY KEY,
    workspace_key VARCHAR(128) NOT NULL,
    name VARCHAR(256) NOT NULL,
    application_id VARCHAR(64) NOT NULL,
    snow_group_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
