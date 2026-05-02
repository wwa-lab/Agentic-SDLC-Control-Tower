CREATE TABLE PLATFORM_AUDIT (
    id VARCHAR(64) PRIMARY KEY,
    event_time TIMESTAMP NOT NULL,
    actor VARCHAR(128) NOT NULL,
    actor_type VARCHAR(32) NOT NULL,
    category VARCHAR(64) NOT NULL,
    action VARCHAR(128) NOT NULL,
    object_type VARCHAR(128) NOT NULL,
    object_id VARCHAR(128) NOT NULL,
    scope_type VARCHAR(32) NOT NULL,
    scope_id VARCHAR(128) NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    evidence_ref VARCHAR(512),
    payload CLOB
);

CREATE INDEX ix_platform_audit_time ON PLATFORM_AUDIT(event_time);
CREATE INDEX ix_platform_audit_scope ON PLATFORM_AUDIT(scope_type, scope_id);
