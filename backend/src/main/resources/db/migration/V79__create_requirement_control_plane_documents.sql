CREATE TABLE requirement_sdd_document_index (
    id VARCHAR(64) PRIMARY KEY,
    requirement_id VARCHAR(20) NOT NULL,
    profile_id VARCHAR(64) NOT NULL,
    sdd_type VARCHAR(64) NOT NULL,
    stage_label VARCHAR(128) NOT NULL,
    title VARCHAR(255) NOT NULL,
    repo_full_name VARCHAR(255) NOT NULL,
    branch_or_ref VARCHAR(128) NOT NULL,
    path VARCHAR(512) NOT NULL,
    latest_commit_sha VARCHAR(128) NOT NULL,
    latest_blob_sha VARCHAR(128) NOT NULL,
    github_url VARCHAR(1024) NOT NULL,
    status VARCHAR(64) NOT NULL,
    indexed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_req_sdd_doc_requirement FOREIGN KEY (requirement_id) REFERENCES requirement (id)
);

INSERT INTO requirement_sdd_document_index (
    id, requirement_id, profile_id, sdd_type, stage_label, title, repo_full_name, branch_or_ref, path, latest_commit_sha, latest_blob_sha, github_url, status, indexed_at
) VALUES
    ('DOC-REQ-0001-REQ', 'REQ-0001', 'standard-sdd', 'requirement', 'Requirement', 'SSO Requirement', 'wwa-lab/payment-gateway-pro', 'main', 'docs/01-requirements/sso-requirement.md', 'c0ffee1001', 'blob-req-0001', 'https://github.com/wwa-lab/payment-gateway-pro/blob/main/docs/01-requirements/sso-requirement.md', 'APPROVED', TIMESTAMP WITH TIME ZONE '2026-04-16 09:05:00+00:00'),
    ('DOC-REQ-0001-STORY', 'REQ-0001', 'standard-sdd', 'user-story', 'User Stories', 'SSO User Stories', 'wwa-lab/payment-gateway-pro', 'main', 'docs/02-user-stories/sso-stories.md', 'c0ffee1002', 'blob-story-0001', 'https://github.com/wwa-lab/payment-gateway-pro/blob/main/docs/02-user-stories/sso-stories.md', 'APPROVED', TIMESTAMP WITH TIME ZONE '2026-04-16 09:06:00+00:00'),
    ('DOC-REQ-0001-SPEC', 'REQ-0001', 'standard-sdd', 'spec', 'Spec', 'SSO Functional Spec', 'wwa-lab/payment-gateway-pro', 'main', 'docs/03-spec/sso-functional-spec.md', 'c0ffee1003', 'blob-spec-0001-v2', 'https://github.com/wwa-lab/payment-gateway-pro/blob/main/docs/03-spec/sso-functional-spec.md', 'IN_REVIEW', TIMESTAMP WITH TIME ZONE '2026-04-16 09:07:00+00:00'),
    ('DOC-REQ-0005-REQ', 'REQ-0005', 'standard-sdd', 'requirement', 'Requirement', 'Audit Trail Requirement', 'wwa-lab/sdlc-tower', 'main', 'docs/01-requirements/audit-trail.md', 'c0ffee5001', 'blob-req-0005', 'https://github.com/wwa-lab/sdlc-tower/blob/main/docs/01-requirements/audit-trail.md', 'APPROVED', TIMESTAMP WITH TIME ZONE '2026-04-16 08:05:00+00:00');
