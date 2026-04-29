CREATE TABLE project_sdd_workspace (
    id VARCHAR(64) PRIMARY KEY,
    application_id VARCHAR(64) NOT NULL,
    application_name VARCHAR(255) NOT NULL,
    snow_group VARCHAR(128) NOT NULL,
    source_repo_full_name VARCHAR(255) NOT NULL,
    sdd_repo_full_name VARCHAR(255) NOT NULL,
    base_branch VARCHAR(128) NOT NULL,
    working_branch VARCHAR(255) NOT NULL,
    lifecycle_status VARCHAR(64) NOT NULL,
    docs_root VARCHAR(128) NOT NULL,
    release_pr_url VARCHAR(1024),
    kb_repo_full_name VARCHAR(255) NOT NULL,
    kb_main_branch VARCHAR(128) NOT NULL,
    kb_preview_branch VARCHAR(255) NOT NULL,
    graph_manifest_path VARCHAR(512) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE requirement_sdd_document_index ADD COLUMN sdd_workspace_id VARCHAR(64);

INSERT INTO project_sdd_workspace (
    id, application_id, application_name, snow_group, source_repo_full_name,
    sdd_repo_full_name, base_branch, working_branch, lifecycle_status,
    docs_root, release_pr_url, kb_repo_full_name, kb_main_branch,
    kb_preview_branch, graph_manifest_path, created_at, updated_at
) VALUES
    (
        'SDDW-PAY-2026-SSO',
        'payment-gateway',
        'Payment Gateway',
        'APAC-PAYMENTS-L2',
        'wwa-lab/payment-gateway-service',
        'wwa-lab/payment-gateway-sdd',
        'main',
        'project/PAY-2026-sso-upgrade',
        'IN_DEVELOPMENT',
        'docs/',
        NULL,
        'wwa-lab/payment-gateway-knowledge-base',
        'main',
        'project/PAY-2026-sso-upgrade',
        '_graph/manifest.json',
        TIMESTAMP WITH TIME ZONE '2026-04-16 09:00:00+00:00',
        TIMESTAMP WITH TIME ZONE '2026-04-16 09:00:00+00:00'
    ),
    (
        'SDDW-TOWER-2026-AUDIT',
        'sdlc-tower',
        'SDLC Control Tower',
        'PLATFORM-ENGINEERING',
        'wwa-lab/sdlc-tower',
        'wwa-lab/sdlc-tower-sdd',
        'main',
        'project/TOWER-2026-audit-trail',
        'IN_DEVELOPMENT',
        'docs/',
        NULL,
        'wwa-lab/sdlc-tower-knowledge-base',
        'main',
        'project/TOWER-2026-audit-trail',
        '_graph/manifest.json',
        TIMESTAMP WITH TIME ZONE '2026-04-16 08:00:00+00:00',
        TIMESTAMP WITH TIME ZONE '2026-04-16 08:00:00+00:00'
    );

UPDATE requirement_sdd_document_index
SET sdd_workspace_id = 'SDDW-PAY-2026-SSO',
    repo_full_name = 'wwa-lab/payment-gateway-sdd',
    branch_or_ref = 'project/PAY-2026-sso-upgrade',
    github_url = REPLACE(github_url, 'wwa-lab/payment-gateway-pro/blob/main', 'wwa-lab/payment-gateway-sdd/blob/project/PAY-2026-sso-upgrade')
WHERE requirement_id = 'REQ-0001';

UPDATE requirement_sdd_document_index
SET sdd_workspace_id = 'SDDW-TOWER-2026-AUDIT',
    repo_full_name = 'wwa-lab/sdlc-tower-sdd',
    branch_or_ref = 'project/TOWER-2026-audit-trail',
    github_url = REPLACE(github_url, 'wwa-lab/sdlc-tower/blob/main', 'wwa-lab/sdlc-tower-sdd/blob/project/TOWER-2026-audit-trail')
WHERE requirement_id = 'REQ-0005';
