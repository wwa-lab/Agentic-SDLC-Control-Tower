CREATE TABLE SUPPORT_REQUEST (
    request_id        VARCHAR(64) PRIMARY KEY,
    request_date      DATE NOT NULL,
    title             VARCHAR(512) NOT NULL,
    category          VARCHAR(64) NOT NULL,
    description       CLOB NOT NULL,
    route             VARCHAR(512),
    reporter_staff_id VARCHAR(64),
    reporter_mode     VARCHAR(32),
    status            VARCHAR(32) NOT NULL,
    jira_key          VARCHAR(64),
    jira_url          VARCHAR(1024),
    payload_json      CLOB,
    attempt_count     INTEGER DEFAULT 0 NOT NULL,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL,
    last_attempt_at   TIMESTAMP,
    next_retry_at     TIMESTAMP
);

CREATE INDEX ix_support_request_status
    ON SUPPORT_REQUEST(status, next_retry_at);

CREATE INDEX ix_support_request_date
    ON SUPPORT_REQUEST(request_date);
