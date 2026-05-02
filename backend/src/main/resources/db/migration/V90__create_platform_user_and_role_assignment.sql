CREATE TABLE PLATFORM_USER (
    staff_id VARCHAR(64) PRIMARY KEY,
    display_name VARCHAR(256) NOT NULL,
    staff_name VARCHAR(256),
    avatar_url VARCHAR(1024),
    email VARCHAR(256),
    profile_source VARCHAR(32) DEFAULT 'manual' NOT NULL,
    last_profile_sync_at TIMESTAMP,
    status VARCHAR(32) NOT NULL,
    password_hash VARCHAR(512),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE PLATFORM_ROLE_ASSIGNMENT (
    id VARCHAR(128) PRIMARY KEY,
    staff_id VARCHAR(64) NOT NULL,
    user_display_name VARCHAR(256) NOT NULL,
    role VARCHAR(64) NOT NULL,
    scope_type VARCHAR(32) NOT NULL,
    scope_id VARCHAR(128) NOT NULL,
    granted_by VARCHAR(64) NOT NULL,
    granted_at TIMESTAMP NOT NULL,
    attributes_json CLOB
);

CREATE UNIQUE INDEX ux_platform_role_assignment
    ON PLATFORM_ROLE_ASSIGNMENT(staff_id, role, scope_type, scope_id);
