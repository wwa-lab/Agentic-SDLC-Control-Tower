CREATE TABLE requirement_source_reference (
    id VARCHAR(64) PRIMARY KEY,
    requirement_id VARCHAR(20) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    external_id VARCHAR(128),
    title VARCHAR(255) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    source_updated_at TIMESTAMP WITH TIME ZONE,
    fetched_at TIMESTAMP WITH TIME ZONE,
    freshness_status VARCHAR(64) NOT NULL,
    error_message CLOB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_req_source_requirement FOREIGN KEY (requirement_id) REFERENCES requirement (id)
);

INSERT INTO requirement_source_reference (
    id, requirement_id, source_type, external_id, title, url, source_updated_at, fetched_at, freshness_status, error_message, created_at
) VALUES
    ('SRC-REQ-0001-JIRA', 'REQ-0001', 'JIRA', 'AUTH-123', 'SSO rollout Jira epic', 'jira://AUTH-123', TIMESTAMP WITH TIME ZONE '2026-04-16 08:12:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 09:00:00+00:00', 'FRESH', NULL, TIMESTAMP WITH TIME ZONE '2026-04-16 09:00:00+00:00'),
    ('SRC-REQ-0001-CONF', 'REQ-0001', 'CONFLUENCE', 'AUTH-SSO', 'Enterprise SSO business notes', 'confluence://AUTH-SSO', TIMESTAMP WITH TIME ZONE '2026-04-15 12:30:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 09:02:00+00:00', 'FRESH', NULL, TIMESTAMP WITH TIME ZONE '2026-04-16 09:02:00+00:00'),
    ('SRC-REQ-0005-GH', 'REQ-0005', 'GITHUB', 'issue-88', 'Audit compliance GitHub issue', 'https://github.com/wwa-lab/sdlc-tower/issues/88', TIMESTAMP WITH TIME ZONE '2026-04-16 07:45:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 08:00:00+00:00', 'FRESH', NULL, TIMESTAMP WITH TIME ZONE '2026-04-16 08:00:00+00:00');
