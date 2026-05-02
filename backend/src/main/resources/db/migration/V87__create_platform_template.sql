CREATE TABLE PLATFORM_TEMPLATE (
    id VARCHAR(64) PRIMARY KEY,
    template_key VARCHAR(128) NOT NULL,
    kind VARCHAR(64) NOT NULL,
    name VARCHAR(256) NOT NULL,
    status VARCHAR(32) NOT NULL,
    owner_id VARCHAR(64) NOT NULL,
    current_version_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE PLATFORM_TEMPLATE_VERSION (
    id VARCHAR(64) PRIMARY KEY,
    template_id VARCHAR(64) NOT NULL,
    version_number INTEGER NOT NULL,
    body CLOB,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(64) NOT NULL
);
