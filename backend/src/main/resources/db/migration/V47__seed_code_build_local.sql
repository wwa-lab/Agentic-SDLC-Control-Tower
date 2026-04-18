-- ============================================================
-- Code & Build Management — Local Seed Data
-- ============================================================

-- ------------------------------------------------------------
-- 1. Repos (4 repos, 2 workspaces, 2 projects)
-- ------------------------------------------------------------
INSERT INTO repo (id, github_repo_id, installation_id, full_name, project_id, workspace_id, default_branch, primary_language, description, visibility, external_url, last_activity_at, last_synced_at, created_at, updated_at) VALUES
    ('repo-cb-001', 100001, 'inst-gh-001', 'acme/control-tower-api', 'proj-alpha-001', 'ws-default-001', 'main', 'Java', 'Backend API for the SDLC Control Tower', 'PRIVATE', 'https://github.com/acme/control-tower-api', TIMESTAMP '2026-04-18 08:30:00', TIMESTAMP '2026-04-18 09:00:00', TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-04-18 09:00:00'),
    ('repo-cb-002', 100002, 'inst-gh-001', 'acme/control-tower-ui', 'proj-alpha-001', 'ws-default-001', 'main', 'TypeScript', 'Vue 3 frontend for the SDLC Control Tower', 'PRIVATE', 'https://github.com/acme/control-tower-ui', TIMESTAMP '2026-04-18 07:45:00', TIMESTAMP '2026-04-18 09:00:00', TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-04-18 09:00:00'),
    ('repo-cb-003', 100003, 'inst-gh-002', 'acme/data-pipeline', 'proj-beta-001', 'ws-enterprise-001', 'main', 'Python', 'ETL pipeline for analytics and reporting', 'INTERNAL', 'https://github.com/acme/data-pipeline', TIMESTAMP '2026-04-17 22:10:00', TIMESTAMP '2026-04-18 09:00:00', TIMESTAMP '2026-03-10 14:00:00', TIMESTAMP '2026-04-18 09:00:00'),
    ('repo-cb-004', 100004, 'inst-gh-002', 'acme/infra-modules', 'proj-beta-001', 'ws-enterprise-001', 'main', 'HCL', 'Terraform modules for cloud infrastructure', 'PRIVATE', 'https://github.com/acme/infra-modules', TIMESTAMP '2026-04-16 16:00:00', TIMESTAMP '2026-04-18 09:00:00', TIMESTAMP '2026-02-15 09:00:00', TIMESTAMP '2026-04-18 09:00:00');

-- ------------------------------------------------------------
-- 2. Branches (12 across 4 repos)
-- ------------------------------------------------------------
INSERT INTO branch (id, repo_id, name, head_sha, ahead_of_default, behind_default, last_commit_at) VALUES
    ('branch-cb-001', 'repo-cb-001', 'main',                    'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2', 0, 0, TIMESTAMP '2026-04-18 08:30:00'),
    ('branch-cb-002', 'repo-cb-001', 'feature/auth-gateway',    'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', 5, 1, TIMESTAMP '2026-04-18 07:00:00'),
    ('branch-cb-003', 'repo-cb-001', 'fix/token-refresh',       'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', 2, 0, TIMESTAMP '2026-04-17 16:30:00'),
    ('branch-cb-004', 'repo-cb-002', 'main',                    'd4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5', 0, 0, TIMESTAMP '2026-04-18 07:45:00'),
    ('branch-cb-005', 'repo-cb-002', 'feature/dashboard-v2',    'e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6', 8, 2, TIMESTAMP '2026-04-18 06:15:00'),
    ('branch-cb-006', 'repo-cb-002', 'chore/deps-update',       'f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1', 1, 0, TIMESTAMP '2026-04-16 14:00:00'),
    ('branch-cb-007', 'repo-cb-003', 'main',                    'a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8', 0, 0, TIMESTAMP '2026-04-17 22:10:00'),
    ('branch-cb-008', 'repo-cb-003', 'feature/incremental-load','b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9', 3, 1, TIMESTAMP '2026-04-17 20:00:00'),
    ('branch-cb-009', 'repo-cb-003', 'fix/schema-drift',        'c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0', 1, 0, TIMESTAMP '2026-04-17 18:30:00'),
    ('branch-cb-010', 'repo-cb-004', 'main',                    'd0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1', 0, 0, TIMESTAMP '2026-04-16 16:00:00'),
    ('branch-cb-011', 'repo-cb-004', 'feature/vpc-peering',     'e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2', 4, 0, TIMESTAMP '2026-04-16 15:00:00'),
    ('branch-cb-012', 'repo-cb-004', 'hotfix/sg-rules',         'f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7', 1, 0, TIMESTAMP '2026-04-15 12:00:00');

-- ------------------------------------------------------------
-- 3. Commits (~80 across 4 repos)
-- ------------------------------------------------------------

-- repo-cb-001 (control-tower-api) — 25 commits
INSERT INTO git_commit (id, repo_id, sha, short_sha, author, message, committed_at, branch_name) VALUES
    ('commit-cb-001', 'repo-cb-001', 'a100000000000000000000000000000000000001', 'a10000000001', 'leo', 'feat: add workspace context endpoint', TIMESTAMP '2026-04-01 09:00:00', 'main'),
    ('commit-cb-002', 'repo-cb-001', 'a100000000000000000000000000000000000002', 'a10000000002', 'leo', 'feat: add project listing API', TIMESTAMP '2026-04-02 10:30:00', 'main'),
    ('commit-cb-003', 'repo-cb-001', 'a100000000000000000000000000000000000003', 'a10000000003', 'alex', 'fix: null pointer in workspace mapper', TIMESTAMP '2026-04-03 14:00:00', 'main'),
    ('commit-cb-004', 'repo-cb-001', 'a100000000000000000000000000000000000004', 'a10000000004', 'leo', 'refactor: extract shared error handler', TIMESTAMP '2026-04-04 11:15:00', 'main'),
    ('commit-cb-005', 'repo-cb-001', 'a100000000000000000000000000000000000005', 'a10000000005', 'alex', 'feat: add health check actuator\n\nStory-Id: STORY-AUTH-001', TIMESTAMP '2026-04-05 09:45:00', 'main'),
    ('commit-cb-006', 'repo-cb-001', 'a100000000000000000000000000000000000006', 'a10000000006', 'leo', 'test: add workspace integration tests', TIMESTAMP '2026-04-06 16:00:00', 'main'),
    ('commit-cb-007', 'repo-cb-001', 'a100000000000000000000000000000000000007', 'a10000000007', 'maya', 'docs: update API README', TIMESTAMP '2026-04-07 08:30:00', 'main'),
    ('commit-cb-008', 'repo-cb-001', 'a100000000000000000000000000000000000008', 'a10000000008', 'leo', 'feat: add auth gateway filter\n\nStory-Id: STORY-AUTH-002', TIMESTAMP '2026-04-08 10:00:00', 'feature/auth-gateway'),
    ('commit-cb-009', 'repo-cb-001', 'a100000000000000000000000000000000000009', 'a10000000009', 'leo', 'feat: add JWT validation service', TIMESTAMP '2026-04-08 14:00:00', 'feature/auth-gateway'),
    ('commit-cb-010', 'repo-cb-001', 'a100000000000000000000000000000000000010', 'a10000000010', 'alex', 'test: add auth gateway unit tests', TIMESTAMP '2026-04-09 09:00:00', 'feature/auth-gateway'),
    ('commit-cb-011', 'repo-cb-001', 'a100000000000000000000000000000000000011', 'a10000000011', 'leo', 'fix: handle expired refresh tokens gracefully', TIMESTAMP '2026-04-10 11:30:00', 'fix/token-refresh'),
    ('commit-cb-012', 'repo-cb-001', 'a100000000000000000000000000000000000012', 'a10000000012', 'leo', 'chore: bump Spring Boot to 3.4.5', TIMESTAMP '2026-04-11 08:00:00', 'main'),
    ('commit-cb-013', 'repo-cb-001', 'a100000000000000000000000000000000000013', 'a10000000013', 'alex', 'feat: add rate limiting middleware', TIMESTAMP '2026-04-12 10:15:00', 'main'),
    ('commit-cb-014', 'repo-cb-001', 'a100000000000000000000000000000000000014', 'a10000000014', 'maya', 'fix: CORS configuration for staging', TIMESTAMP '2026-04-13 15:00:00', 'main'),
    ('commit-cb-015', 'repo-cb-001', 'a100000000000000000000000000000000000015', 'a10000000015', 'leo', 'feat: add Flyway migration V20', TIMESTAMP '2026-04-14 09:30:00', 'main'),
    ('commit-cb-016', 'repo-cb-001', 'a100000000000000000000000000000000000016', 'a10000000016', 'alex', 'refactor: simplify repo layer generics', TIMESTAMP '2026-04-15 10:00:00', 'main'),
    ('commit-cb-017', 'repo-cb-001', 'a100000000000000000000000000000000000017', 'a10000000017', 'leo', 'feat: add pipeline webhook receiver\n\nStory-Id: STORY-PHANTOM-001', TIMESTAMP '2026-04-15 14:30:00', 'main'),
    ('commit-cb-018', 'repo-cb-001', 'a100000000000000000000000000000000000018', 'a10000000018', 'leo', 'feat: add design artifact endpoint', TIMESTAMP '2026-04-16 09:00:00', 'main'),
    ('commit-cb-019', 'repo-cb-001', 'a100000000000000000000000000000000000019', 'a10000000019', 'maya', 'test: add design artifact service tests', TIMESTAMP '2026-04-16 14:00:00', 'main'),
    ('commit-cb-020', 'repo-cb-001', 'a100000000000000000000000000000000000020', 'a10000000020', 'alex', 'fix: pagination off-by-one in project list', TIMESTAMP '2026-04-17 08:00:00', 'main'),
    ('commit-cb-021', 'repo-cb-001', 'a100000000000000000000000000000000000021', 'a10000000021', 'leo', 'feat: add testing management controller', TIMESTAMP '2026-04-17 11:00:00', 'main'),
    ('commit-cb-022', 'repo-cb-001', 'a100000000000000000000000000000000000022', 'a10000000022', 'leo', 'feat: add report center export endpoint', TIMESTAMP '2026-04-17 16:00:00', 'main'),
    ('commit-cb-023', 'repo-cb-001', 'a100000000000000000000000000000000000023', 'a10000000023', 'alex', 'chore: update Checkstyle rules', TIMESTAMP '2026-04-18 07:00:00', 'main'),
    ('commit-cb-024', 'repo-cb-001', 'a100000000000000000000000000000000000024', 'a10000000024', 'leo', 'feat: add auth gateway rate limiter', TIMESTAMP '2026-04-18 08:00:00', 'feature/auth-gateway'),
    ('commit-cb-025', 'repo-cb-001', 'a100000000000000000000000000000000000025', 'a10000000025', 'maya', 'fix: token refresh race condition', TIMESTAMP '2026-04-18 08:30:00', 'fix/token-refresh');

-- repo-cb-002 (control-tower-ui) — 20 commits
INSERT INTO git_commit (id, repo_id, sha, short_sha, author, message, committed_at, branch_name) VALUES
    ('commit-cb-026', 'repo-cb-002', 'b200000000000000000000000000000000000001', 'b20000000001', 'sara', 'feat: scaffold Vue 3 app shell', TIMESTAMP '2026-04-01 10:00:00', 'main'),
    ('commit-cb-027', 'repo-cb-002', 'b200000000000000000000000000000000000002', 'b20000000002', 'sara', 'feat: add dashboard layout component', TIMESTAMP '2026-04-02 11:00:00', 'main'),
    ('commit-cb-028', 'repo-cb-002', 'b200000000000000000000000000000000000003', 'b20000000003', 'tom', 'feat: add sidebar navigation', TIMESTAMP '2026-04-03 09:15:00', 'main'),
    ('commit-cb-029', 'repo-cb-002', 'b200000000000000000000000000000000000004', 'b20000000004', 'sara', 'feat: add project list view', TIMESTAMP '2026-04-04 14:30:00', 'main'),
    ('commit-cb-030', 'repo-cb-002', 'b200000000000000000000000000000000000005', 'b20000000005', 'tom', 'fix: router guard redirect loop', TIMESTAMP '2026-04-05 10:00:00', 'main'),
    ('commit-cb-031', 'repo-cb-002', 'b200000000000000000000000000000000000006', 'b20000000006', 'sara', 'feat: add Pinia workspace store', TIMESTAMP '2026-04-06 09:00:00', 'main'),
    ('commit-cb-032', 'repo-cb-002', 'b200000000000000000000000000000000000007', 'b20000000007', 'tom', 'chore: configure Vitest and coverage', TIMESTAMP '2026-04-07 11:30:00', 'main'),
    ('commit-cb-033', 'repo-cb-002', 'b200000000000000000000000000000000000008', 'b20000000008', 'sara', 'feat: add dashboard v2 widget grid\n\nStory-Id: STORY-PHANTOM-002', TIMESTAMP '2026-04-08 10:00:00', 'feature/dashboard-v2'),
    ('commit-cb-034', 'repo-cb-002', 'b200000000000000000000000000000000000009', 'b20000000009', 'tom', 'feat: add risk signal sparkline component', TIMESTAMP '2026-04-09 14:00:00', 'feature/dashboard-v2'),
    ('commit-cb-035', 'repo-cb-002', 'b200000000000000000000000000000000000010', 'b20000000010', 'sara', 'test: add workspace store unit tests', TIMESTAMP '2026-04-10 09:30:00', 'main'),
    ('commit-cb-036', 'repo-cb-002', 'b200000000000000000000000000000000000011', 'b20000000011', 'tom', 'feat: add design management page', TIMESTAMP '2026-04-11 11:00:00', 'main'),
    ('commit-cb-037', 'repo-cb-002', 'b200000000000000000000000000000000000012', 'b20000000012', 'sara', 'fix: dark mode toggle persistence', TIMESTAMP '2026-04-12 15:00:00', 'main'),
    ('commit-cb-038', 'repo-cb-002', 'b200000000000000000000000000000000000013', 'b20000000013', 'tom', 'feat: add testing management list view', TIMESTAMP '2026-04-13 10:00:00', 'main'),
    ('commit-cb-039', 'repo-cb-002', 'b200000000000000000000000000000000000014', 'b20000000014', 'sara', 'feat: add report center chart components', TIMESTAMP '2026-04-14 09:00:00', 'main'),
    ('commit-cb-040', 'repo-cb-002', 'b200000000000000000000000000000000000015', 'b20000000015', 'tom', 'refactor: extract shared table component', TIMESTAMP '2026-04-15 13:00:00', 'main'),
    ('commit-cb-041', 'repo-cb-002', 'b200000000000000000000000000000000000016', 'b20000000016', 'sara', 'chore: update Vite to 6.2', TIMESTAMP '2026-04-16 08:30:00', 'main'),
    ('commit-cb-042', 'repo-cb-002', 'b200000000000000000000000000000000000017', 'b20000000017', 'tom', 'feat: add dashboard v2 filter bar', TIMESTAMP '2026-04-17 10:00:00', 'feature/dashboard-v2'),
    ('commit-cb-043', 'repo-cb-002', 'b200000000000000000000000000000000000018', 'b20000000018', 'sara', 'fix: responsive breakpoints for sidebar', TIMESTAMP '2026-04-17 14:00:00', 'main'),
    ('commit-cb-044', 'repo-cb-002', 'b200000000000000000000000000000000000019', 'b20000000019', 'tom', 'chore: update deps to latest patch', TIMESTAMP '2026-04-16 14:00:00', 'chore/deps-update'),
    ('commit-cb-045', 'repo-cb-002', 'b200000000000000000000000000000000000020', 'b20000000020', 'sara', 'feat: add code build management placeholder', TIMESTAMP '2026-04-18 07:45:00', 'main');

-- repo-cb-003 (data-pipeline) — 20 commits
INSERT INTO git_commit (id, repo_id, sha, short_sha, author, message, committed_at, branch_name) VALUES
    ('commit-cb-046', 'repo-cb-003', 'c300000000000000000000000000000000000001', 'c30000000001', 'jin', 'feat: add base ETL runner', TIMESTAMP '2026-04-01 11:00:00', 'main'),
    ('commit-cb-047', 'repo-cb-003', 'c300000000000000000000000000000000000002', 'c30000000002', 'jin', 'feat: add source connector registry', TIMESTAMP '2026-04-02 13:00:00', 'main'),
    ('commit-cb-048', 'repo-cb-003', 'c300000000000000000000000000000000000003', 'c30000000003', 'nadia', 'feat: add S3 sink connector', TIMESTAMP '2026-04-03 10:30:00', 'main'),
    ('commit-cb-049', 'repo-cb-003', 'c300000000000000000000000000000000000004', 'c30000000004', 'jin', 'fix: handle null partition key', TIMESTAMP '2026-04-04 15:00:00', 'main'),
    ('commit-cb-050', 'repo-cb-003', 'c300000000000000000000000000000000000005', 'c30000000005', 'nadia', 'test: add connector integration tests', TIMESTAMP '2026-04-05 09:00:00', 'main'),
    ('commit-cb-051', 'repo-cb-003', 'c300000000000000000000000000000000000006', 'c30000000006', 'jin', 'feat: add incremental load strategy', TIMESTAMP '2026-04-06 14:30:00', 'feature/incremental-load'),
    ('commit-cb-052', 'repo-cb-003', 'c300000000000000000000000000000000000007', 'c30000000007', 'nadia', 'feat: add watermark tracking', TIMESTAMP '2026-04-07 10:00:00', 'feature/incremental-load'),
    ('commit-cb-053', 'repo-cb-003', 'c300000000000000000000000000000000000008', 'c30000000008', 'jin', 'refactor: extract transform pipeline', TIMESTAMP '2026-04-08 11:00:00', 'main'),
    ('commit-cb-054', 'repo-cb-003', 'c300000000000000000000000000000000000009', 'c30000000009', 'nadia', 'fix: schema drift detection', TIMESTAMP '2026-04-09 13:00:00', 'fix/schema-drift'),
    ('commit-cb-055', 'repo-cb-003', 'c300000000000000000000000000000000000010', 'c30000000010', 'jin', 'feat: add dead letter queue handler', TIMESTAMP '2026-04-10 09:30:00', 'main'),
    ('commit-cb-056', 'repo-cb-003', 'c300000000000000000000000000000000000011', 'c30000000011', 'nadia', 'test: add schema drift regression tests', TIMESTAMP '2026-04-11 14:00:00', 'main'),
    ('commit-cb-057', 'repo-cb-003', 'c300000000000000000000000000000000000012', 'c30000000012', 'jin', 'feat: add metrics exporter', TIMESTAMP '2026-04-12 10:00:00', 'main'),
    ('commit-cb-058', 'repo-cb-003', 'c300000000000000000000000000000000000013', 'c30000000013', 'nadia', 'chore: update boto3 and pyarrow', TIMESTAMP '2026-04-13 08:00:00', 'main'),
    ('commit-cb-059', 'repo-cb-003', 'c300000000000000000000000000000000000014', 'c30000000014', 'jin', 'feat: add retry with exponential backoff', TIMESTAMP '2026-04-14 11:30:00', 'main'),
    ('commit-cb-060', 'repo-cb-003', 'c300000000000000000000000000000000000015', 'c30000000015', 'nadia', 'fix: memory leak in batch accumulator', TIMESTAMP '2026-04-15 09:00:00', 'main'),
    ('commit-cb-061', 'repo-cb-003', 'c300000000000000000000000000000000000016', 'c30000000016', 'jin', 'test: add load runner benchmark', TIMESTAMP '2026-04-16 14:00:00', 'main'),
    ('commit-cb-062', 'repo-cb-003', 'c300000000000000000000000000000000000017', 'c30000000017', 'nadia', 'feat: add partition pruning', TIMESTAMP '2026-04-17 10:30:00', 'main'),
    ('commit-cb-063', 'repo-cb-003', 'c300000000000000000000000000000000000018', 'c30000000018', 'jin', 'feat: add incremental checkpoint', TIMESTAMP '2026-04-17 20:00:00', 'feature/incremental-load'),
    ('commit-cb-064', 'repo-cb-003', 'c300000000000000000000000000000000000019', 'c30000000019', 'nadia', 'fix: schema drift false positive on nullable cols', TIMESTAMP '2026-04-17 18:30:00', 'fix/schema-drift'),
    ('commit-cb-065', 'repo-cb-003', 'c300000000000000000000000000000000000020', 'c30000000020', 'jin', 'refactor: consolidate config loader', TIMESTAMP '2026-04-17 22:10:00', 'main');

-- repo-cb-004 (infra-modules) — 15 commits
INSERT INTO git_commit (id, repo_id, sha, short_sha, author, message, committed_at, branch_name) VALUES
    ('commit-cb-066', 'repo-cb-004', 'd400000000000000000000000000000000000001', 'd40000000001', 'ops-bot', 'feat: add base VPC module', TIMESTAMP '2026-03-15 10:00:00', 'main'),
    ('commit-cb-067', 'repo-cb-004', 'd400000000000000000000000000000000000002', 'd40000000002', 'ops-bot', 'feat: add EKS cluster module', TIMESTAMP '2026-03-18 14:00:00', 'main'),
    ('commit-cb-068', 'repo-cb-004', 'd400000000000000000000000000000000000003', 'd40000000003', 'kai', 'feat: add RDS Aurora module', TIMESTAMP '2026-03-22 09:30:00', 'main'),
    ('commit-cb-069', 'repo-cb-004', 'd400000000000000000000000000000000000004', 'd40000000004', 'kai', 'fix: security group egress rules', TIMESTAMP '2026-03-25 11:00:00', 'main'),
    ('commit-cb-070', 'repo-cb-004', 'd400000000000000000000000000000000000005', 'd40000000005', 'ops-bot', 'chore: pin provider versions', TIMESTAMP '2026-03-28 08:00:00', 'main'),
    ('commit-cb-071', 'repo-cb-004', 'd400000000000000000000000000000000000006', 'd40000000006', 'kai', 'feat: add VPC peering module', TIMESTAMP '2026-04-01 10:00:00', 'feature/vpc-peering'),
    ('commit-cb-072', 'repo-cb-004', 'd400000000000000000000000000000000000007', 'd40000000007', 'kai', 'feat: add peering route tables', TIMESTAMP '2026-04-04 13:00:00', 'feature/vpc-peering'),
    ('commit-cb-073', 'repo-cb-004', 'd400000000000000000000000000000000000008', 'd40000000008', 'ops-bot', 'test: add VPC peering plan tests', TIMESTAMP '2026-04-07 09:00:00', 'feature/vpc-peering'),
    ('commit-cb-074', 'repo-cb-004', 'd400000000000000000000000000000000000009', 'd40000000009', 'kai', 'fix: peering DNS resolution', TIMESTAMP '2026-04-10 14:30:00', 'feature/vpc-peering'),
    ('commit-cb-075', 'repo-cb-004', 'd400000000000000000000000000000000000010', 'd40000000010', 'ops-bot', 'feat: add CloudWatch alarms module', TIMESTAMP '2026-04-12 10:00:00', 'main'),
    ('commit-cb-076', 'repo-cb-004', 'd400000000000000000000000000000000000011', 'd40000000011', 'kai', 'fix: SG ingress CIDR for staging', TIMESTAMP '2026-04-15 12:00:00', 'hotfix/sg-rules'),
    ('commit-cb-077', 'repo-cb-004', 'd400000000000000000000000000000000000012', 'd40000000012', 'kai', 'refactor: use for_each instead of count', TIMESTAMP '2026-04-15 16:00:00', 'main'),
    ('commit-cb-078', 'repo-cb-004', 'd400000000000000000000000000000000000013', 'd40000000013', 'ops-bot', 'docs: add module usage examples', TIMESTAMP '2026-04-16 09:00:00', 'main'),
    ('commit-cb-079', 'repo-cb-004', 'd400000000000000000000000000000000000014', 'd40000000014', 'kai', 'feat: add WAF module', TIMESTAMP '2026-04-16 14:00:00', 'main'),
    ('commit-cb-080', 'repo-cb-004', 'd400000000000000000000000000000000000015', 'd40000000015', 'ops-bot', 'chore: update Terraform to 1.9', TIMESTAMP '2026-04-16 16:00:00', 'main');

-- ------------------------------------------------------------
-- 4. Pull Requests (20)
-- ------------------------------------------------------------
INSERT INTO pull_request (id, repo_id, pr_number, title, author, source_branch, target_branch, state, head_sha, is_bot_authored, external_url, body_text, created_at, updated_at) VALUES
    ('pr-cb-001', 'repo-cb-001', 101, 'feat: add auth gateway filter', 'leo', 'feature/auth-gateway', 'main', 'OPEN', 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', FALSE, 'https://github.com/acme/control-tower-api/pull/101', 'Adds JWT-based auth gateway with rate limiting.', TIMESTAMP '2026-04-08 10:30:00', TIMESTAMP '2026-04-18 08:00:00'),
    ('pr-cb-002', 'repo-cb-001', 102, 'fix: handle expired refresh tokens', 'leo', 'fix/token-refresh', 'main', 'OPEN', 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', FALSE, 'https://github.com/acme/control-tower-api/pull/102', 'Graceful handling of expired tokens with proper error response.', TIMESTAMP '2026-04-10 12:00:00', TIMESTAMP '2026-04-18 08:30:00'),
    ('pr-cb-003', 'repo-cb-001', 100, 'feat: add health check actuator', 'alex', 'feature/health-check', 'main', 'MERGED', 'a100000000000000000000000000000000000005', FALSE, 'https://github.com/acme/control-tower-api/pull/100', 'Spring Boot Actuator health endpoint.', TIMESTAMP '2026-04-05 10:00:00', TIMESTAMP '2026-04-05 14:00:00'),
    ('pr-cb-004', 'repo-cb-001', 99, 'chore: bump Spring Boot to 3.4.5', 'dependabot[bot]', 'chore/spring-bump', 'main', 'MERGED', 'a100000000000000000000000000000000000012', TRUE, 'https://github.com/acme/control-tower-api/pull/99', 'Automated dependency update.', TIMESTAMP '2026-04-11 06:00:00', TIMESTAMP '2026-04-11 08:00:00'),
    ('pr-cb-005', 'repo-cb-001', 103, 'feat: add rate limiting middleware', 'alex', 'feature/rate-limit', 'main', 'MERGED', 'a100000000000000000000000000000000000013', FALSE, 'https://github.com/acme/control-tower-api/pull/103', 'Token bucket rate limiter for API endpoints.', TIMESTAMP '2026-04-12 10:30:00', TIMESTAMP '2026-04-12 16:00:00'),
    ('pr-cb-006', 'repo-cb-002', 201, 'feat: dashboard v2 widget grid', 'sara', 'feature/dashboard-v2', 'main', 'OPEN', 'b200000000000000000000000000000000000017', FALSE, 'https://github.com/acme/control-tower-ui/pull/201', 'New widget grid layout for dashboard v2.', TIMESTAMP '2026-04-08 10:30:00', TIMESTAMP '2026-04-17 10:00:00'),
    ('pr-cb-007', 'repo-cb-002', 200, 'feat: add sidebar navigation', 'tom', 'feature/sidebar', 'main', 'MERGED', 'b200000000000000000000000000000000000003', FALSE, 'https://github.com/acme/control-tower-ui/pull/200', 'Collapsible sidebar with icon navigation.', TIMESTAMP '2026-04-03 09:30:00', TIMESTAMP '2026-04-03 14:00:00'),
    ('pr-cb-008', 'repo-cb-002', 202, 'chore: update deps to latest patch', 'renovate[bot]', 'chore/deps-update', 'main', 'OPEN', 'b200000000000000000000000000000000000019', TRUE, 'https://github.com/acme/control-tower-ui/pull/202', 'Patch-level updates for Vue and Vite.', TIMESTAMP '2026-04-16 14:30:00', TIMESTAMP '2026-04-16 14:30:00'),
    ('pr-cb-009', 'repo-cb-002', 199, 'fix: router guard redirect loop', 'tom', 'fix/router-guard', 'main', 'MERGED', 'b200000000000000000000000000000000000005', FALSE, 'https://github.com/acme/control-tower-ui/pull/199', 'Fix infinite redirect when token is present but expired.', TIMESTAMP '2026-04-05 10:30:00', TIMESTAMP '2026-04-05 12:00:00'),
    ('pr-cb-010', 'repo-cb-002', 203, 'fix: dark mode toggle persistence', 'sara', 'fix/dark-mode', 'main', 'MERGED', 'b200000000000000000000000000000000000012', FALSE, 'https://github.com/acme/control-tower-ui/pull/203', 'Persist dark mode preference in localStorage.', TIMESTAMP '2026-04-12 15:30:00', TIMESTAMP '2026-04-12 17:00:00'),
    ('pr-cb-011', 'repo-cb-003', 301, 'feat: add incremental load strategy', 'jin', 'feature/incremental-load', 'main', 'OPEN', 'c300000000000000000000000000000000000018', FALSE, 'https://github.com/acme/data-pipeline/pull/301', 'Watermark-based incremental loading for large datasets.', TIMESTAMP '2026-04-06 15:00:00', TIMESTAMP '2026-04-17 20:00:00'),
    ('pr-cb-012', 'repo-cb-003', 302, 'fix: schema drift detection', 'nadia', 'fix/schema-drift', 'main', 'OPEN', 'c300000000000000000000000000000000000019', FALSE, 'https://github.com/acme/data-pipeline/pull/302', 'Fix false positives on nullable column changes.', TIMESTAMP '2026-04-09 13:30:00', TIMESTAMP '2026-04-17 18:30:00'),
    ('pr-cb-013', 'repo-cb-003', 300, 'feat: add S3 sink connector', 'nadia', 'feature/s3-sink', 'main', 'MERGED', 'c300000000000000000000000000000000000003', FALSE, 'https://github.com/acme/data-pipeline/pull/300', 'Parquet-based S3 sink with partitioning.', TIMESTAMP '2026-04-03 11:00:00', TIMESTAMP '2026-04-03 16:00:00'),
    ('pr-cb-014', 'repo-cb-003', 303, 'feat: add dead letter queue handler', 'jin', 'feature/dlq', 'main', 'MERGED', 'c300000000000000000000000000000000000010', FALSE, 'https://github.com/acme/data-pipeline/pull/303', 'Route unprocessable records to DLQ with error metadata.', TIMESTAMP '2026-04-10 10:00:00', TIMESTAMP '2026-04-10 14:00:00'),
    ('pr-cb-015', 'repo-cb-003', 304, 'fix: memory leak in batch accumulator', 'nadia', 'fix/batch-leak', 'main', 'MERGED', 'c300000000000000000000000000000000000015', FALSE, 'https://github.com/acme/data-pipeline/pull/304', 'Ensure batch buffers are released after flush.', TIMESTAMP '2026-04-15 09:30:00', TIMESTAMP '2026-04-15 12:00:00'),
    ('pr-cb-016', 'repo-cb-004', 401, 'feat: add VPC peering module', 'kai', 'feature/vpc-peering', 'main', 'OPEN', 'e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2', FALSE, 'https://github.com/acme/infra-modules/pull/401', 'Cross-account VPC peering with DNS resolution.', TIMESTAMP '2026-04-01 10:30:00', TIMESTAMP '2026-04-16 15:00:00'),
    ('pr-cb-017', 'repo-cb-004', 400, 'feat: add RDS Aurora module', 'kai', 'feature/aurora', 'main', 'MERGED', 'd400000000000000000000000000000000000003', FALSE, 'https://github.com/acme/infra-modules/pull/400', 'Aurora PostgreSQL cluster with read replicas.', TIMESTAMP '2026-03-22 10:00:00', TIMESTAMP '2026-03-22 16:00:00'),
    ('pr-cb-018', 'repo-cb-004', 402, 'hotfix: SG ingress CIDR for staging', 'kai', 'hotfix/sg-rules', 'main', 'MERGED', 'f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7', FALSE, 'https://github.com/acme/infra-modules/pull/402', 'Restrict staging SG to VPN CIDR only.', TIMESTAMP '2026-04-15 12:30:00', TIMESTAMP '2026-04-15 13:00:00'),
    ('pr-cb-019', 'repo-cb-004', 403, 'feat: add WAF module', 'kai', 'feature/waf', 'main', 'MERGED', 'd400000000000000000000000000000000000014', FALSE, 'https://github.com/acme/infra-modules/pull/403', 'AWS WAF with managed rule groups.', TIMESTAMP '2026-04-16 14:30:00', TIMESTAMP '2026-04-16 16:00:00'),
    ('pr-cb-020', 'repo-cb-004', 404, 'chore: update Terraform to 1.9', 'ops-bot', 'chore/tf-upgrade', 'main', 'MERGED', 'd400000000000000000000000000000000000015', TRUE, 'https://github.com/acme/infra-modules/pull/404', 'Terraform version upgrade with state migration.', TIMESTAMP '2026-04-16 16:30:00', TIMESTAMP '2026-04-16 17:00:00');

-- ------------------------------------------------------------
-- 5. Pull Request Reviews
-- ------------------------------------------------------------
INSERT INTO pull_request_review (id, pr_id, reviewer_id, reviewer_name, state, body_summary, submitted_at) VALUES
    ('review-cb-001', 'pr-cb-001', 'u-020', 'alex', 'CHANGES_REQUESTED', 'Rate limiter config should be externalized. JWT validation needs audience check.', TIMESTAMP '2026-04-09 09:00:00'),
    ('review-cb-002', 'pr-cb-001', 'u-021', 'maya', 'APPROVED', 'LGTM after the config changes.', TIMESTAMP '2026-04-18 07:30:00'),
    ('review-cb-003', 'pr-cb-003', 'u-020', 'alex', 'APPROVED', 'Looks good. Health endpoint is minimal and correct.', TIMESTAMP '2026-04-05 13:00:00'),
    ('review-cb-004', 'pr-cb-006', 'u-030', 'tom', 'CHANGES_REQUESTED', 'Widget grid needs responsive breakpoints for mobile.', TIMESTAMP '2026-04-10 11:00:00'),
    ('review-cb-005', 'pr-cb-011', 'u-040', 'nadia', 'APPROVED', 'Watermark tracking is solid. Tests cover edge cases.', TIMESTAMP '2026-04-17 21:00:00'),
    ('review-cb-006', 'pr-cb-016', 'u-050', 'ops-bot', 'COMMENTED', 'Route tables look correct but verify DNS resolution in staging.', TIMESTAMP '2026-04-12 11:00:00'),
    ('review-cb-007', 'pr-cb-018', 'u-020', 'alex', 'APPROVED', 'Hotfix is minimal and scoped correctly.', TIMESTAMP '2026-04-15 12:45:00');

-- ------------------------------------------------------------
-- 6. Pipeline Runs (40)
-- ------------------------------------------------------------
INSERT INTO pipeline_run (id, repo_id, run_number, pipeline_name, trigger, branch, actor, head_sha, status, duration_sec, started_at, completed_at, external_url, github_run_id, created_at) VALUES
    -- repo-cb-001 runs (15)
    ('run-cb-001', 'repo-cb-001', 1001, 'CI Build', 'PUSH', 'main', 'leo', 'a100000000000000000000000000000000000001', 'SUCCESS', 245, TIMESTAMP '2026-04-01 09:05:00', TIMESTAMP '2026-04-01 09:09:05', 'https://github.com/acme/control-tower-api/actions/runs/1001', 9001001, TIMESTAMP '2026-04-01 09:05:00'),
    ('run-cb-002', 'repo-cb-001', 1002, 'CI Build', 'PUSH', 'main', 'alex', 'a100000000000000000000000000000000000003', 'SUCCESS', 238, TIMESTAMP '2026-04-03 14:05:00', TIMESTAMP '2026-04-03 14:08:58', 'https://github.com/acme/control-tower-api/actions/runs/1002', 9001002, TIMESTAMP '2026-04-03 14:05:00'),
    ('run-cb-003', 'repo-cb-001', 1003, 'CI Build', 'PUSH', 'feature/auth-gateway', 'leo', 'a100000000000000000000000000000000000008', 'FAILURE', 180, TIMESTAMP '2026-04-08 10:05:00', TIMESTAMP '2026-04-08 10:08:00', 'https://github.com/acme/control-tower-api/actions/runs/1003', 9001003, TIMESTAMP '2026-04-08 10:05:00'),
    ('run-cb-004', 'repo-cb-001', 1004, 'CI Build', 'PUSH', 'feature/auth-gateway', 'leo', 'a100000000000000000000000000000000000009', 'SUCCESS', 260, TIMESTAMP '2026-04-08 14:05:00', TIMESTAMP '2026-04-08 14:09:20', 'https://github.com/acme/control-tower-api/actions/runs/1004', 9001004, TIMESTAMP '2026-04-08 14:05:00'),
    ('run-cb-005', 'repo-cb-001', 1005, 'CI Build', 'PUSH', 'main', 'leo', 'a100000000000000000000000000000000000012', 'SUCCESS', 230, TIMESTAMP '2026-04-11 08:05:00', TIMESTAMP '2026-04-11 08:08:50', 'https://github.com/acme/control-tower-api/actions/runs/1005', 9001005, TIMESTAMP '2026-04-11 08:05:00'),
    ('run-cb-006', 'repo-cb-001', 1006, 'CI Build', 'PUSH', 'main', 'alex', 'a100000000000000000000000000000000000013', 'SUCCESS', 242, TIMESTAMP '2026-04-12 10:20:00', TIMESTAMP '2026-04-12 10:24:02', 'https://github.com/acme/control-tower-api/actions/runs/1006', 9001006, TIMESTAMP '2026-04-12 10:20:00'),
    ('run-cb-007', 'repo-cb-001', 1007, 'CI Build', 'PUSH', 'main', 'leo', 'a100000000000000000000000000000000000015', 'FAILURE', 310, TIMESTAMP '2026-04-14 09:35:00', TIMESTAMP '2026-04-14 09:40:10', 'https://github.com/acme/control-tower-api/actions/runs/1007', 9001007, TIMESTAMP '2026-04-14 09:35:00'),
    ('run-cb-008', 'repo-cb-001', 1008, 'CI Build', 'PUSH', 'main', 'leo', 'a100000000000000000000000000000000000018', 'SUCCESS', 248, TIMESTAMP '2026-04-16 09:05:00', TIMESTAMP '2026-04-16 09:09:08', 'https://github.com/acme/control-tower-api/actions/runs/1008', 9001008, TIMESTAMP '2026-04-16 09:05:00'),
    ('run-cb-009', 'repo-cb-001', 1009, 'CI Build', 'PUSH', 'main', 'alex', 'a100000000000000000000000000000000000020', 'SUCCESS', 235, TIMESTAMP '2026-04-17 08:05:00', TIMESTAMP '2026-04-17 08:08:55', 'https://github.com/acme/control-tower-api/actions/runs/1009', 9001009, TIMESTAMP '2026-04-17 08:05:00'),
    ('run-cb-010', 'repo-cb-001', 1010, 'CI Build', 'PUSH', 'main', 'leo', 'a100000000000000000000000000000000000021', 'IN_PROGRESS', NULL, TIMESTAMP '2026-04-18 08:35:00', NULL, 'https://github.com/acme/control-tower-api/actions/runs/1010', 9001010, TIMESTAMP '2026-04-18 08:35:00'),
    ('run-cb-011', 'repo-cb-001', 1011, 'Security Scan', 'SCHEDULE', 'main', 'github-actions', 'a100000000000000000000000000000000000023', 'SUCCESS', 420, TIMESTAMP '2026-04-18 02:00:00', TIMESTAMP '2026-04-18 02:07:00', 'https://github.com/acme/control-tower-api/actions/runs/1011', 9001011, TIMESTAMP '2026-04-18 02:00:00'),
    ('run-cb-012', 'repo-cb-001', 1012, 'CI Build', 'PULL_REQUEST', 'feature/auth-gateway', 'leo', 'a100000000000000000000000000000000000024', 'FAILURE', 195, TIMESTAMP '2026-04-18 08:05:00', TIMESTAMP '2026-04-18 08:08:15', 'https://github.com/acme/control-tower-api/actions/runs/1012', 9001012, TIMESTAMP '2026-04-18 08:05:00'),
    ('run-cb-013', 'repo-cb-001', 1013, 'CI Build', 'PULL_REQUEST', 'fix/token-refresh', 'maya', 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', 'SUCCESS', 240, TIMESTAMP '2026-04-18 08:35:00', TIMESTAMP '2026-04-18 08:39:00', 'https://github.com/acme/control-tower-api/actions/runs/1013', 9001013, TIMESTAMP '2026-04-18 08:35:00'),
    ('run-cb-014', 'repo-cb-001', 1014, 'Deploy Staging', 'WORKFLOW_DISPATCH', 'main', 'leo', 'a100000000000000000000000000000000000023', 'CANCELLED', 60, TIMESTAMP '2026-04-18 07:10:00', TIMESTAMP '2026-04-18 07:11:00', 'https://github.com/acme/control-tower-api/actions/runs/1014', 9001014, TIMESTAMP '2026-04-18 07:10:00'),
    ('run-cb-015', 'repo-cb-001', 1015, 'CI Build', 'PUSH', 'main', 'alex', 'a100000000000000000000000000000000000023', 'SUCCESS', 228, TIMESTAMP '2026-04-18 07:05:00', TIMESTAMP '2026-04-18 07:08:48', 'https://github.com/acme/control-tower-api/actions/runs/1015', 9001015, TIMESTAMP '2026-04-18 07:05:00'),
    -- repo-cb-002 runs (10)
    ('run-cb-016', 'repo-cb-002', 2001, 'CI Build', 'PUSH', 'main', 'sara', 'b200000000000000000000000000000000000001', 'SUCCESS', 120, TIMESTAMP '2026-04-01 10:05:00', TIMESTAMP '2026-04-01 10:07:00', 'https://github.com/acme/control-tower-ui/actions/runs/2001', 9002001, TIMESTAMP '2026-04-01 10:05:00'),
    ('run-cb-017', 'repo-cb-002', 2002, 'CI Build', 'PUSH', 'main', 'tom', 'b200000000000000000000000000000000000003', 'SUCCESS', 115, TIMESTAMP '2026-04-03 09:20:00', TIMESTAMP '2026-04-03 09:21:55', 'https://github.com/acme/control-tower-ui/actions/runs/2002', 9002002, TIMESTAMP '2026-04-03 09:20:00'),
    ('run-cb-018', 'repo-cb-002', 2003, 'CI Build', 'PUSH', 'feature/dashboard-v2', 'sara', 'b200000000000000000000000000000000000008', 'FAILURE', 95, TIMESTAMP '2026-04-08 10:05:00', TIMESTAMP '2026-04-08 10:06:35', 'https://github.com/acme/control-tower-ui/actions/runs/2003', 9002003, TIMESTAMP '2026-04-08 10:05:00'),
    ('run-cb-019', 'repo-cb-002', 2004, 'CI Build', 'PUSH', 'feature/dashboard-v2', 'tom', 'b200000000000000000000000000000000000009', 'SUCCESS', 118, TIMESTAMP '2026-04-09 14:05:00', TIMESTAMP '2026-04-09 14:06:58', 'https://github.com/acme/control-tower-ui/actions/runs/2004', 9002004, TIMESTAMP '2026-04-09 14:05:00'),
    ('run-cb-020', 'repo-cb-002', 2005, 'CI Build', 'PUSH', 'main', 'sara', 'b200000000000000000000000000000000000010', 'SUCCESS', 122, TIMESTAMP '2026-04-10 09:35:00', TIMESTAMP '2026-04-10 09:37:02', 'https://github.com/acme/control-tower-ui/actions/runs/2005', 9002005, TIMESTAMP '2026-04-10 09:35:00'),
    ('run-cb-021', 'repo-cb-002', 2006, 'CI Build', 'PUSH', 'main', 'tom', 'b200000000000000000000000000000000000013', 'SUCCESS', 110, TIMESTAMP '2026-04-13 10:05:00', TIMESTAMP '2026-04-13 10:06:50', 'https://github.com/acme/control-tower-ui/actions/runs/2006', 9002006, TIMESTAMP '2026-04-13 10:05:00'),
    ('run-cb-022', 'repo-cb-002', 2007, 'CI Build', 'PUSH', 'main', 'sara', 'b200000000000000000000000000000000000016', 'FAILURE', 88, TIMESTAMP '2026-04-16 08:35:00', TIMESTAMP '2026-04-16 08:36:28', 'https://github.com/acme/control-tower-ui/actions/runs/2007', 9002007, TIMESTAMP '2026-04-16 08:35:00'),
    ('run-cb-023', 'repo-cb-002', 2008, 'CI Build', 'PUSH', 'feature/dashboard-v2', 'tom', 'b200000000000000000000000000000000000017', 'SUCCESS', 125, TIMESTAMP '2026-04-17 10:05:00', TIMESTAMP '2026-04-17 10:07:05', 'https://github.com/acme/control-tower-ui/actions/runs/2008', 9002008, TIMESTAMP '2026-04-17 10:05:00'),
    ('run-cb-024', 'repo-cb-002', 2009, 'CI Build', 'PUSH', 'main', 'sara', 'b200000000000000000000000000000000000018', 'SUCCESS', 112, TIMESTAMP '2026-04-17 14:05:00', TIMESTAMP '2026-04-17 14:06:52', 'https://github.com/acme/control-tower-ui/actions/runs/2009', 9002009, TIMESTAMP '2026-04-17 14:05:00'),
    ('run-cb-025', 'repo-cb-002', 2010, 'E2E Playwright', 'PULL_REQUEST', 'feature/dashboard-v2', 'sara', 'b200000000000000000000000000000000000017', 'IN_PROGRESS', NULL, TIMESTAMP '2026-04-18 08:40:00', NULL, 'https://github.com/acme/control-tower-ui/actions/runs/2010', 9002010, TIMESTAMP '2026-04-18 08:40:00'),
    -- repo-cb-003 runs (10)
    ('run-cb-026', 'repo-cb-003', 3001, 'CI Build', 'PUSH', 'main', 'jin', 'c300000000000000000000000000000000000001', 'SUCCESS', 310, TIMESTAMP '2026-04-01 11:05:00', TIMESTAMP '2026-04-01 11:10:10', 'https://github.com/acme/data-pipeline/actions/runs/3001', 9003001, TIMESTAMP '2026-04-01 11:05:00'),
    ('run-cb-027', 'repo-cb-003', 3002, 'CI Build', 'PUSH', 'main', 'nadia', 'c300000000000000000000000000000000000003', 'SUCCESS', 298, TIMESTAMP '2026-04-03 10:35:00', TIMESTAMP '2026-04-03 10:39:58', 'https://github.com/acme/data-pipeline/actions/runs/3002', 9003002, TIMESTAMP '2026-04-03 10:35:00'),
    ('run-cb-028', 'repo-cb-003', 3003, 'CI Build', 'PUSH', 'feature/incremental-load', 'jin', 'c300000000000000000000000000000000000006', 'FAILURE', 265, TIMESTAMP '2026-04-06 14:35:00', TIMESTAMP '2026-04-06 14:39:25', 'https://github.com/acme/data-pipeline/actions/runs/3003', 9003003, TIMESTAMP '2026-04-06 14:35:00'),
    ('run-cb-029', 'repo-cb-003', 3004, 'CI Build', 'PUSH', 'feature/incremental-load', 'nadia', 'c300000000000000000000000000000000000007', 'SUCCESS', 305, TIMESTAMP '2026-04-07 10:05:00', TIMESTAMP '2026-04-07 10:10:05', 'https://github.com/acme/data-pipeline/actions/runs/3004', 9003004, TIMESTAMP '2026-04-07 10:05:00'),
    ('run-cb-030', 'repo-cb-003', 3005, 'CI Build', 'PUSH', 'main', 'jin', 'c300000000000000000000000000000000000010', 'SUCCESS', 290, TIMESTAMP '2026-04-10 09:35:00', TIMESTAMP '2026-04-10 09:39:50', 'https://github.com/acme/data-pipeline/actions/runs/3005', 9003005, TIMESTAMP '2026-04-10 09:35:00'),
    ('run-cb-031', 'repo-cb-003', 3006, 'CI Build', 'PUSH', 'main', 'nadia', 'c300000000000000000000000000000000000015', 'SUCCESS', 278, TIMESTAMP '2026-04-15 09:05:00', TIMESTAMP '2026-04-15 09:09:38', 'https://github.com/acme/data-pipeline/actions/runs/3006', 9003006, TIMESTAMP '2026-04-15 09:05:00'),
    ('run-cb-032', 'repo-cb-003', 3007, 'CI Build', 'PUSH', 'main', 'jin', 'c300000000000000000000000000000000000016', 'FAILURE', 340, TIMESTAMP '2026-04-16 14:05:00', TIMESTAMP '2026-04-16 14:10:40', 'https://github.com/acme/data-pipeline/actions/runs/3007', 9003007, TIMESTAMP '2026-04-16 14:05:00'),
    ('run-cb-033', 'repo-cb-003', 3008, 'CI Build', 'PUSH', 'main', 'nadia', 'c300000000000000000000000000000000000017', 'SUCCESS', 288, TIMESTAMP '2026-04-17 10:35:00', TIMESTAMP '2026-04-17 10:40:23', 'https://github.com/acme/data-pipeline/actions/runs/3008', 9003008, TIMESTAMP '2026-04-17 10:35:00'),
    ('run-cb-034', 'repo-cb-003', 3009, 'CI Build', 'PUSH', 'feature/incremental-load', 'jin', 'c300000000000000000000000000000000000018', 'SUCCESS', 315, TIMESTAMP '2026-04-17 20:05:00', TIMESTAMP '2026-04-17 20:10:20', 'https://github.com/acme/data-pipeline/actions/runs/3009', 9003009, TIMESTAMP '2026-04-17 20:05:00'),
    ('run-cb-035', 'repo-cb-003', 3010, 'Security Scan', 'SCHEDULE', 'main', 'github-actions', 'c300000000000000000000000000000000000020', 'CANCELLED', 45, TIMESTAMP '2026-04-18 02:00:00', TIMESTAMP '2026-04-18 02:00:45', 'https://github.com/acme/data-pipeline/actions/runs/3010', 9003010, TIMESTAMP '2026-04-18 02:00:00'),
    -- repo-cb-004 runs (5)
    ('run-cb-036', 'repo-cb-004', 4001, 'Terraform Plan', 'PUSH', 'main', 'kai', 'd400000000000000000000000000000000000003', 'SUCCESS', 180, TIMESTAMP '2026-03-22 09:35:00', TIMESTAMP '2026-03-22 09:38:00', 'https://github.com/acme/infra-modules/actions/runs/4001', 9004001, TIMESTAMP '2026-03-22 09:35:00'),
    ('run-cb-037', 'repo-cb-004', 4002, 'Terraform Plan', 'PUSH', 'feature/vpc-peering', 'kai', 'd400000000000000000000000000000000000006', 'FAILURE', 150, TIMESTAMP '2026-04-01 10:05:00', TIMESTAMP '2026-04-01 10:07:30', 'https://github.com/acme/infra-modules/actions/runs/4002', 9004002, TIMESTAMP '2026-04-01 10:05:00'),
    ('run-cb-038', 'repo-cb-004', 4003, 'Terraform Plan', 'PUSH', 'feature/vpc-peering', 'kai', 'd400000000000000000000000000000000000009', 'SUCCESS', 175, TIMESTAMP '2026-04-10 14:35:00', TIMESTAMP '2026-04-10 14:37:55', 'https://github.com/acme/infra-modules/actions/runs/4003', 9004003, TIMESTAMP '2026-04-10 14:35:00'),
    ('run-cb-039', 'repo-cb-004', 4004, 'Terraform Plan', 'PUSH', 'hotfix/sg-rules', 'kai', 'f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7', 'SUCCESS', 165, TIMESTAMP '2026-04-15 12:05:00', TIMESTAMP '2026-04-15 12:07:45', 'https://github.com/acme/infra-modules/actions/runs/4004', 9004004, TIMESTAMP '2026-04-15 12:05:00'),
    ('run-cb-040', 'repo-cb-004', 4005, 'Terraform Plan', 'PUSH', 'main', 'ops-bot', 'd400000000000000000000000000000000000015', 'SUCCESS', 170, TIMESTAMP '2026-04-16 16:05:00', TIMESTAMP '2026-04-16 16:07:50', 'https://github.com/acme/infra-modules/actions/runs/4005', 9004005, TIMESTAMP '2026-04-16 16:05:00');

-- ------------------------------------------------------------
-- 7. Pipeline Jobs (representative subset)
-- ------------------------------------------------------------
INSERT INTO pipeline_job (id, run_id, name, status, conclusion, job_number, started_at, completed_at) VALUES
    -- run-cb-003 (FAILURE)
    ('job-cb-001', 'run-cb-003', 'build', 'COMPLETED', 'SUCCESS', 1, TIMESTAMP '2026-04-08 10:05:10', TIMESTAMP '2026-04-08 10:06:30'),
    ('job-cb-002', 'run-cb-003', 'test', 'COMPLETED', 'FAILURE', 2, TIMESTAMP '2026-04-08 10:06:35', TIMESTAMP '2026-04-08 10:08:00'),
    -- run-cb-007 (FAILURE)
    ('job-cb-003', 'run-cb-007', 'build', 'COMPLETED', 'SUCCESS', 1, TIMESTAMP '2026-04-14 09:35:10', TIMESTAMP '2026-04-14 09:37:00'),
    ('job-cb-004', 'run-cb-007', 'test', 'COMPLETED', 'SUCCESS', 2, TIMESTAMP '2026-04-14 09:37:05', TIMESTAMP '2026-04-14 09:38:30'),
    ('job-cb-005', 'run-cb-007', 'integration-test', 'COMPLETED', 'FAILURE', 3, TIMESTAMP '2026-04-14 09:38:35', TIMESTAMP '2026-04-14 09:40:10'),
    -- run-cb-012 (FAILURE)
    ('job-cb-006', 'run-cb-012', 'build', 'COMPLETED', 'SUCCESS', 1, TIMESTAMP '2026-04-18 08:05:10', TIMESTAMP '2026-04-18 08:06:20'),
    ('job-cb-007', 'run-cb-012', 'test', 'COMPLETED', 'FAILURE', 2, TIMESTAMP '2026-04-18 08:06:25', TIMESTAMP '2026-04-18 08:08:15'),
    -- run-cb-018 (FAILURE - UI)
    ('job-cb-008', 'run-cb-018', 'lint', 'COMPLETED', 'SUCCESS', 1, TIMESTAMP '2026-04-08 10:05:05', TIMESTAMP '2026-04-08 10:05:30'),
    ('job-cb-009', 'run-cb-018', 'unit-test', 'COMPLETED', 'FAILURE', 2, TIMESTAMP '2026-04-08 10:05:35', TIMESTAMP '2026-04-08 10:06:35'),
    -- run-cb-028 (FAILURE - pipeline)
    ('job-cb-010', 'run-cb-028', 'lint', 'COMPLETED', 'SUCCESS', 1, TIMESTAMP '2026-04-06 14:35:10', TIMESTAMP '2026-04-06 14:36:00'),
    ('job-cb-011', 'run-cb-028', 'test', 'COMPLETED', 'FAILURE', 2, TIMESTAMP '2026-04-06 14:36:05', TIMESTAMP '2026-04-06 14:39:25'),
    -- run-cb-037 (FAILURE - infra)
    ('job-cb-012', 'run-cb-037', 'terraform-plan', 'COMPLETED', 'FAILURE', 1, TIMESTAMP '2026-04-01 10:05:05', TIMESTAMP '2026-04-01 10:07:30'),
    -- run-cb-001 (SUCCESS - representative)
    ('job-cb-013', 'run-cb-001', 'build', 'COMPLETED', 'SUCCESS', 1, TIMESTAMP '2026-04-01 09:05:10', TIMESTAMP '2026-04-01 09:06:30'),
    ('job-cb-014', 'run-cb-001', 'test', 'COMPLETED', 'SUCCESS', 2, TIMESTAMP '2026-04-01 09:06:35', TIMESTAMP '2026-04-01 09:08:20'),
    ('job-cb-015', 'run-cb-001', 'integration-test', 'COMPLETED', 'SUCCESS', 3, TIMESTAMP '2026-04-01 09:08:25', TIMESTAMP '2026-04-01 09:09:05');

-- ------------------------------------------------------------
-- 8. Pipeline Steps (representative subset)
-- ------------------------------------------------------------
INSERT INTO pipeline_step (id, job_id, name, order_index, conclusion, started_at, completed_at) VALUES
    -- job-cb-002 (test FAILURE in run-cb-003)
    ('step-cb-001', 'job-cb-002', 'Checkout', 1, 'SUCCESS', TIMESTAMP '2026-04-08 10:06:35', TIMESTAMP '2026-04-08 10:06:40'),
    ('step-cb-002', 'job-cb-002', 'Setup JDK 21', 2, 'SUCCESS', TIMESTAMP '2026-04-08 10:06:40', TIMESTAMP '2026-04-08 10:06:55'),
    ('step-cb-003', 'job-cb-002', 'Run unit tests', 3, 'FAILURE', TIMESTAMP '2026-04-08 10:06:55', TIMESTAMP '2026-04-08 10:08:00'),
    -- job-cb-005 (integration-test FAILURE in run-cb-007)
    ('step-cb-004', 'job-cb-005', 'Checkout', 1, 'SUCCESS', TIMESTAMP '2026-04-14 09:38:35', TIMESTAMP '2026-04-14 09:38:40'),
    ('step-cb-005', 'job-cb-005', 'Start Testcontainers', 2, 'SUCCESS', TIMESTAMP '2026-04-14 09:38:40', TIMESTAMP '2026-04-14 09:39:10'),
    ('step-cb-006', 'job-cb-005', 'Run integration tests', 3, 'FAILURE', TIMESTAMP '2026-04-14 09:39:10', TIMESTAMP '2026-04-14 09:40:10'),
    -- job-cb-007 (test FAILURE in run-cb-012)
    ('step-cb-007', 'job-cb-007', 'Checkout', 1, 'SUCCESS', TIMESTAMP '2026-04-18 08:06:25', TIMESTAMP '2026-04-18 08:06:30'),
    ('step-cb-008', 'job-cb-007', 'Setup JDK 21', 2, 'SUCCESS', TIMESTAMP '2026-04-18 08:06:30', TIMESTAMP '2026-04-18 08:06:45'),
    ('step-cb-009', 'job-cb-007', 'Run unit tests', 3, 'FAILURE', TIMESTAMP '2026-04-18 08:06:45', TIMESTAMP '2026-04-18 08:08:15'),
    -- job-cb-009 (unit-test FAILURE in run-cb-018)
    ('step-cb-010', 'job-cb-009', 'Checkout', 1, 'SUCCESS', TIMESTAMP '2026-04-08 10:05:35', TIMESTAMP '2026-04-08 10:05:40'),
    ('step-cb-011', 'job-cb-009', 'Install deps', 2, 'SUCCESS', TIMESTAMP '2026-04-08 10:05:40', TIMESTAMP '2026-04-08 10:06:00'),
    ('step-cb-012', 'job-cb-009', 'Run vitest', 3, 'FAILURE', TIMESTAMP '2026-04-08 10:06:00', TIMESTAMP '2026-04-08 10:06:35'),
    -- job-cb-011 (test FAILURE in run-cb-028)
    ('step-cb-013', 'job-cb-011', 'Checkout', 1, 'SUCCESS', TIMESTAMP '2026-04-06 14:36:05', TIMESTAMP '2026-04-06 14:36:10'),
    ('step-cb-014', 'job-cb-011', 'Install deps', 2, 'SUCCESS', TIMESTAMP '2026-04-06 14:36:10', TIMESTAMP '2026-04-06 14:36:45'),
    ('step-cb-015', 'job-cb-011', 'Run pytest', 3, 'FAILURE', TIMESTAMP '2026-04-06 14:36:45', TIMESTAMP '2026-04-06 14:39:25'),
    -- job-cb-012 (terraform-plan FAILURE in run-cb-037)
    ('step-cb-016', 'job-cb-012', 'Checkout', 1, 'SUCCESS', TIMESTAMP '2026-04-01 10:05:05', TIMESTAMP '2026-04-01 10:05:10'),
    ('step-cb-017', 'job-cb-012', 'Setup Terraform', 2, 'SUCCESS', TIMESTAMP '2026-04-01 10:05:10', TIMESTAMP '2026-04-01 10:05:25'),
    ('step-cb-018', 'job-cb-012', 'Terraform plan', 3, 'FAILURE', TIMESTAMP '2026-04-01 10:05:25', TIMESTAMP '2026-04-01 10:07:30');

-- ------------------------------------------------------------
-- 9. Log Excerpts (failing steps with pre-redacted secrets)
-- ------------------------------------------------------------
INSERT INTO log_excerpt (id, step_id, text, byte_count, external_url) VALUES
    ('log-cb-001', 'step-cb-003', 'AuthGatewayFilterTest > shouldRejectExpiredToken FAILED
  java.lang.AssertionError: Expected status 401 but was 500
    at com.sdlctower.auth.AuthGatewayFilterTest.shouldRejectExpiredToken(AuthGatewayFilterTest.java:42)
  Caused by: io.jsonwebtoken.ExpiredJwtException: JWT expired at 2026-04-08T09:00:00Z
  REDACTED: token=*****, secret=*****
  1 test failed, 14 passed', 384, 'https://github.com/acme/control-tower-api/actions/runs/1003/jobs/2/logs'),
    ('log-cb-002', 'step-cb-006', 'WorkspaceRepositoryIT > shouldFindByProjectId FAILED
  org.springframework.dao.DataIntegrityViolationException: could not execute statement
    at org.hibernate.exception.ConstraintViolationException
  Caused by: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException:
    Unique index or primary key violation: "UQ_WORKSPACE_NAME"
  REDACTED: dsn=*****, password=*****
  3 tests failed, 22 passed', 420, 'https://github.com/acme/control-tower-api/actions/runs/1007/jobs/3/logs'),
    ('log-cb-003', 'step-cb-009', 'AuthGatewayRateLimiterTest > shouldThrottleAboveLimit FAILED
  java.lang.AssertionError: Expected 429 Too Many Requests but got 200 OK
    at com.sdlctower.auth.AuthGatewayRateLimiterTest.shouldThrottleAboveLimit(AuthGatewayRateLimiterTest.java:58)
  REDACTED: api_key=*****
  1 test failed, 16 passed', 310, 'https://github.com/acme/control-tower-api/actions/runs/1012/jobs/2/logs'),
    ('log-cb-004', 'step-cb-012', 'FAIL src/components/DashboardWidgetGrid.spec.ts
  DashboardWidgetGrid > renders widget cards
    TypeError: Cannot read properties of undefined (reading ''map'')
      at DashboardWidgetGrid.vue:24
  REDACTED: VITE_API_KEY=*****
  1 test failed, 8 passed', 280, 'https://github.com/acme/control-tower-ui/actions/runs/2003/jobs/2/logs'),
    ('log-cb-005', 'step-cb-015', 'FAILED tests/test_incremental_loader.py::test_watermark_rollback
  AssertionError: watermark was not rolled back after partial failure
    assert datetime(2026, 4, 5) == datetime(2026, 4, 3)
  REDACTED: AWS_SECRET_ACCESS_KEY=*****
  1 failed, 12 passed', 295, 'https://github.com/acme/data-pipeline/actions/runs/3003/jobs/2/logs'),
    ('log-cb-006', 'step-cb-018', 'Error: Invalid reference "module.vpc_peering.route_table_id"
  on modules/peering/main.tf line 42:
    42:   route_table_id = module.vpc_peering.route_table_id
  Module "vpc_peering" does not have an output named "route_table_id".
  Did you mean "route_table_ids"?
  REDACTED: AWS_ACCESS_KEY_ID=*****', 340, 'https://github.com/acme/infra-modules/actions/runs/4002/jobs/1/logs');

-- ------------------------------------------------------------
-- 10. Check Runs
-- ------------------------------------------------------------
INSERT INTO check_run (id, repo_id, head_sha, name, status, conclusion, external_url, started_at, completed_at) VALUES
    ('check-cb-001', 'repo-cb-001', 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', 'CI Build', 'COMPLETED', 'FAILURE', 'https://github.com/acme/control-tower-api/actions/runs/1012', TIMESTAMP '2026-04-18 08:05:00', TIMESTAMP '2026-04-18 08:08:15'),
    ('check-cb-002', 'repo-cb-001', 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', 'Security Scan', 'COMPLETED', 'SUCCESS', 'https://github.com/acme/control-tower-api/actions/runs/1011', TIMESTAMP '2026-04-18 02:00:00', TIMESTAMP '2026-04-18 02:07:00'),
    ('check-cb-003', 'repo-cb-001', 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', 'CI Build', 'COMPLETED', 'SUCCESS', 'https://github.com/acme/control-tower-api/actions/runs/1013', TIMESTAMP '2026-04-18 08:35:00', TIMESTAMP '2026-04-18 08:39:00'),
    ('check-cb-004', 'repo-cb-002', 'b200000000000000000000000000000000000017', 'CI Build', 'COMPLETED', 'SUCCESS', 'https://github.com/acme/control-tower-ui/actions/runs/2008', TIMESTAMP '2026-04-17 10:05:00', TIMESTAMP '2026-04-17 10:07:05'),
    ('check-cb-005', 'repo-cb-002', 'b200000000000000000000000000000000000017', 'E2E Playwright', 'IN_PROGRESS', NULL, 'https://github.com/acme/control-tower-ui/actions/runs/2010', TIMESTAMP '2026-04-18 08:40:00', NULL);

-- ------------------------------------------------------------
-- 11. Commit Story Links
-- ------------------------------------------------------------
INSERT INTO commit_story_link (id, commit_id, story_id, link_status, candidate_story_id) VALUES
    ('csl-cb-001', 'commit-cb-005', 'STORY-AUTH-001', 'KNOWN', NULL),
    ('csl-cb-002', 'commit-cb-008', 'STORY-AUTH-002', 'KNOWN', NULL),
    ('csl-cb-003', 'commit-cb-017', 'STORY-PHANTOM-001', 'UNKNOWN_STORY', 'STORY-PHANTOM-001'),
    ('csl-cb-004', 'commit-cb-033', 'STORY-PHANTOM-002', 'UNKNOWN_STORY', 'STORY-PHANTOM-002'),
    ('csl-cb-005', 'commit-cb-001', NULL, 'NO_STORY_ID', NULL),
    ('csl-cb-006', 'commit-cb-002', NULL, 'NO_STORY_ID', NULL),
    ('csl-cb-007', 'commit-cb-003', NULL, 'NO_STORY_ID', NULL),
    ('csl-cb-008', 'commit-cb-046', NULL, 'NO_STORY_ID', NULL);

-- ------------------------------------------------------------
-- 12. PR Story Links
-- ------------------------------------------------------------
INSERT INTO pr_story_link (id, pr_id, story_id, link_status, candidate_story_id) VALUES
    ('psl-cb-001', 'pr-cb-001', 'STORY-AUTH-002', 'KNOWN', NULL),
    ('psl-cb-002', 'pr-cb-003', 'STORY-AUTH-001', 'KNOWN', NULL),
    ('psl-cb-003', 'pr-cb-006', 'STORY-PHANTOM-002', 'UNKNOWN_STORY', 'STORY-PHANTOM-002'),
    ('psl-cb-004', 'pr-cb-011', NULL, 'NO_STORY_ID', NULL),
    ('psl-cb-005', 'pr-cb-002', 'STORY-AUTH-001', 'AMBIGUOUS', 'STORY-AUTH-003');

-- ------------------------------------------------------------
-- 13. AI PR Reviews (4 reviews)
-- ------------------------------------------------------------
INSERT INTO ai_pr_review (id, pr_id, head_sha, skill_version, status, generated_at, notes_json, error_json, invalidated_at) VALUES
    ('airev-cb-001', 'pr-cb-001', 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', 'pr-reviewer-v2.1', 'COMPLETED', TIMESTAMP '2026-04-18 07:45:00', NULL, NULL, NULL),
    ('airev-cb-002', 'pr-cb-006', 'b200000000000000000000000000000000000017', 'pr-reviewer-v2.1', 'COMPLETED', TIMESTAMP '2026-04-17 10:30:00', NULL, NULL, NULL),
    ('airev-cb-003', 'pr-cb-011', 'c300000000000000000000000000000000000018', 'pr-reviewer-v2.1', 'PENDING', NULL, NULL, NULL, NULL),
    ('airev-cb-004', 'pr-cb-012', 'c300000000000000000000000000000000000019', 'pr-reviewer-v2.1', 'FAILED', NULL, NULL, '{"code":"TIMEOUT","message":"Review generation timed out after 120s"}', NULL);

-- AI PR Review Notes (2 BLOCKER + 3 MAJOR + 4 MINOR for airev-cb-001)
INSERT INTO ai_pr_review_note (id, review_id, severity, file_path, start_line, end_line, message) VALUES
    ('note-cb-001', 'airev-cb-001', 'BLOCKER', 'src/main/java/com/sdlctower/auth/AuthGatewayFilter.java', 42, 48, 'JWT secret is loaded from application.yml without encryption. Use Vault or environment variable injection.'),
    ('note-cb-002', 'airev-cb-001', 'BLOCKER', 'src/main/java/com/sdlctower/auth/AuthGatewayFilter.java', 65, 65, 'Missing audience claim validation. Tokens from other services will be accepted.'),
    ('note-cb-003', 'airev-cb-001', 'MAJOR', 'src/main/java/com/sdlctower/auth/JwtValidationService.java', 30, 35, 'Clock skew tolerance of 5 minutes is excessive. Recommend 30 seconds max.'),
    ('note-cb-004', 'airev-cb-001', 'MAJOR', 'src/main/java/com/sdlctower/auth/RateLimiterConfig.java', 12, 18, 'Rate limit values are hardcoded. Should be configurable per environment.'),
    ('note-cb-005', 'airev-cb-001', 'MAJOR', 'src/main/java/com/sdlctower/auth/AuthGatewayFilter.java', 80, 85, 'Error response body leaks internal exception message. Return a generic message.'),
    ('note-cb-006', 'airev-cb-001', 'MINOR', 'src/main/java/com/sdlctower/auth/AuthGatewayFilter.java', 10, 10, 'Unused import: java.util.stream.Collectors'),
    ('note-cb-007', 'airev-cb-001', 'MINOR', 'src/main/java/com/sdlctower/auth/JwtValidationService.java', 1, 5, 'Missing class-level Javadoc.'),
    ('note-cb-008', 'airev-cb-001', 'MINOR', 'src/test/java/com/sdlctower/auth/AuthGatewayFilterTest.java', 20, 22, 'Test method name does not follow project naming convention.'),
    ('note-cb-009', 'airev-cb-001', 'MINOR', 'src/main/java/com/sdlctower/auth/RateLimiterConfig.java', 5, 5, 'Consider using @ConfigurationProperties instead of @Value annotations.');

-- Clean review for airev-cb-002 (no notes = clean)

-- ------------------------------------------------------------
-- 14. AI Triage Rows (6, including 1 FAILED_EVIDENCE)
-- ------------------------------------------------------------
INSERT INTO ai_triage_row (id, run_id, step_id, skill_version, attempt_number, status, likely_cause, candidate_owners_json, confidence, evidence_json, error_json, generated_at) VALUES
    ('triage-cb-001', 'run-cb-003', 'step-cb-003', 'triage-v1.0', 1, 'COMPLETED', 'Expired JWT token handling returns 500 instead of 401. The AuthGatewayFilter catches ExpiredJwtException but re-throws it as a generic RuntimeException.', '["leo","alex"]', 0.92, '{"log_lines":["AuthGatewayFilterTest.java:42","ExpiredJwtException"],"pattern":"exception_rethrow"}', NULL, TIMESTAMP '2026-04-08 10:15:00'),
    ('triage-cb-002', 'run-cb-007', 'step-cb-006', 'triage-v1.0', 1, 'COMPLETED', 'Unique constraint violation on workspace name. Test data setup creates duplicate workspace entries across test methods without proper cleanup.', '["alex","maya"]', 0.87, '{"log_lines":["UQ_WORKSPACE_NAME","DataIntegrityViolationException"],"pattern":"test_isolation"}', NULL, TIMESTAMP '2026-04-14 09:45:00'),
    ('triage-cb-003', 'run-cb-012', 'step-cb-009', 'triage-v1.0', 1, 'COMPLETED', 'Rate limiter test expects 429 but gets 200. The rate limiter bean is not being loaded in the test slice because it requires a custom AutoConfiguration.', '["leo"]', 0.78, '{"log_lines":["AuthGatewayRateLimiterTest.java:58","Expected 429"],"pattern":"missing_bean"}', NULL, TIMESTAMP '2026-04-18 08:15:00'),
    ('triage-cb-004', 'run-cb-018', 'step-cb-012', 'triage-v1.0', 1, 'COMPLETED', 'DashboardWidgetGrid component accesses .map on undefined prop. The widgets prop defaults to undefined instead of an empty array.', '["sara","tom"]', 0.95, '{"log_lines":["DashboardWidgetGrid.vue:24","Cannot read properties of undefined"],"pattern":"undefined_prop"}', NULL, TIMESTAMP '2026-04-08 10:10:00'),
    ('triage-cb-005', 'run-cb-028', 'step-cb-015', 'triage-v1.0', 1, 'COMPLETED', 'Watermark rollback assertion fails. After a partial batch failure the watermark advances to the failed batch timestamp instead of rolling back to the last successful checkpoint.', '["jin","nadia"]', 0.85, '{"log_lines":["test_watermark_rollback","assert datetime(2026, 4, 5) == datetime(2026, 4, 3)"],"pattern":"state_rollback"}', NULL, TIMESTAMP '2026-04-06 14:45:00'),
    ('triage-cb-006', 'run-cb-037', 'step-cb-018', 'triage-v1.0', 1, 'FAILED_EVIDENCE', NULL, NULL, NULL, NULL, '{"code":"PARSE_ERROR","message":"Could not extract structured output from Terraform plan error log"}', TIMESTAMP '2026-04-01 10:10:00');

-- ------------------------------------------------------------
-- 15. AI Workspace Summaries (4)
-- ------------------------------------------------------------
INSERT INTO ai_workspace_summary (id, workspace_id, repo_id, skill_version, status, generated_at, narrative, evidence_json) VALUES
    ('awsum-cb-001', 'ws-default-001', 'repo-cb-001', 'summary-v1.0', 'COMPLETED', TIMESTAMP '2026-04-18 09:00:00', 'The API repository shows steady progress with 23 commits over the past 18 days. Two feature branches are active: auth-gateway (5 commits, 1 open PR with 2 BLOCKER-level AI review notes) and token-refresh (2 commits, 1 open PR passing CI). The main branch has had 2 CI failures in the last week, both related to test isolation issues. Security scan is passing. Recommended actions: address the BLOCKER notes on PR #101 before merge, and investigate the recurring test isolation failures.', '{"open_prs":2,"merged_prs":3,"failures_7d":2,"coverage_trend":"stable"}'),
    ('awsum-cb-002', 'ws-default-001', 'repo-cb-002', 'summary-v1.0', 'COMPLETED', TIMESTAMP '2026-04-18 09:00:00', 'The frontend repository has 20 commits with active dashboard-v2 work. The dashboard-v2 branch (8 ahead, 2 behind) has an open PR with pending E2E tests. One CI failure on the main branch was caused by a Vite version bump that broke type checks. The deps-update PR from renovate bot is waiting for review. Overall velocity is healthy with consistent daily commits.', '{"open_prs":2,"merged_prs":3,"failures_7d":1,"coverage_trend":"improving"}'),
    ('awsum-cb-003', 'ws-enterprise-001', 'repo-cb-003', 'summary-v1.0', 'COMPLETED', TIMESTAMP '2026-04-18 09:00:00', 'The data pipeline repository is the most active in the enterprise workspace with 20 commits. Two open PRs target incremental loading and schema drift fixes. The incremental-load feature had an early test failure (watermark rollback) that was resolved in a subsequent commit. One security scan was cancelled. The batch accumulator memory leak fix (PR #304) was successfully merged.', '{"open_prs":2,"merged_prs":3,"failures_7d":1,"coverage_trend":"stable"}'),
    ('awsum-cb-004', 'ws-enterprise-001', 'repo-cb-004', 'summary-v1.0', 'COMPLETED', TIMESTAMP '2026-04-18 09:00:00', 'The infrastructure modules repository has 15 commits with the VPC peering feature as the main active work. An early Terraform plan failure (output name mismatch) was resolved. The SG hotfix was merged promptly. Terraform was upgraded to 1.9. The repository has the lowest commit frequency but all recent plan runs are passing.', '{"open_prs":1,"merged_prs":4,"failures_7d":0,"coverage_trend":"n/a"}');

-- ------------------------------------------------------------
-- 16. Change Log Entries
-- ------------------------------------------------------------
INSERT INTO code_build_change_log (id, entity_type, entity_id, entry_type, actor_id, detail, created_at) VALUES
    ('cblog-001', 'REPO', 'repo-cb-001', 'REPO_SYNCED', 'system', 'Full sync completed. 25 commits, 3 branches, 5 PRs ingested.', TIMESTAMP '2026-04-18 09:00:00'),
    ('cblog-002', 'REPO', 'repo-cb-002', 'REPO_SYNCED', 'system', 'Full sync completed. 20 commits, 3 branches, 5 PRs ingested.', TIMESTAMP '2026-04-18 09:00:00'),
    ('cblog-003', 'REPO', 'repo-cb-003', 'REPO_SYNCED', 'system', 'Full sync completed. 20 commits, 3 branches, 5 PRs ingested.', TIMESTAMP '2026-04-18 09:00:00'),
    ('cblog-004', 'REPO', 'repo-cb-004', 'REPO_SYNCED', 'system', 'Full sync completed. 15 commits, 3 branches, 5 PRs ingested.', TIMESTAMP '2026-04-18 09:00:00'),
    ('cblog-005', 'PIPELINE_RUN', 'run-cb-003', 'RUN_FAILED', 'system', 'CI Build failed on feature/auth-gateway. 1 test failure in AuthGatewayFilterTest.', TIMESTAMP '2026-04-08 10:08:00'),
    ('cblog-006', 'PIPELINE_RUN', 'run-cb-007', 'RUN_FAILED', 'system', 'CI Build failed on main. 3 integration test failures due to constraint violation.', TIMESTAMP '2026-04-14 09:40:10'),
    ('cblog-007', 'PIPELINE_RUN', 'run-cb-012', 'RUN_FAILED', 'system', 'CI Build failed on feature/auth-gateway. Rate limiter test failure.', TIMESTAMP '2026-04-18 08:08:15'),
    ('cblog-008', 'AI_PR_REVIEW', 'airev-cb-001', 'REVIEW_GENERATED', 'system', 'AI review completed for PR #101. Found 2 BLOCKER, 3 MAJOR, 4 MINOR notes.', TIMESTAMP '2026-04-18 07:45:00'),
    ('cblog-009', 'AI_TRIAGE', 'triage-cb-001', 'TRIAGE_COMPLETED', 'system', 'Triage completed for run-cb-003 step-cb-003. Confidence: 0.92. Cause: exception rethrow pattern.', TIMESTAMP '2026-04-08 10:15:00'),
    ('cblog-010', 'AI_TRIAGE', 'triage-cb-006', 'TRIAGE_FAILED', 'system', 'Triage failed for run-cb-037. Could not parse Terraform plan error log.', TIMESTAMP '2026-04-01 10:10:00'),
    ('cblog-011', 'STORY_LINK', 'csl-cb-003', 'UNKNOWN_STORY_DETECTED', 'system', 'Commit commit-cb-017 references STORY-PHANTOM-001 which is not in the requirements registry.', TIMESTAMP '2026-04-15 14:35:00'),
    ('cblog-012', 'STORY_LINK', 'psl-cb-005', 'AMBIGUOUS_LINK_DETECTED', 'system', 'PR pr-cb-002 references STORY-AUTH-001 but candidate STORY-AUTH-003 also matches.', TIMESTAMP '2026-04-10 12:05:00');

-- ------------------------------------------------------------
-- 17. Ingestion Outbox (sample entries)
-- ------------------------------------------------------------
INSERT INTO ingestion_outbox (id, delivery_id, event_type, raw_body, received_at, status, error_payload, processed_at) VALUES
    ('outbox-cb-001', 'del-gh-001', 'push', '{"ref":"refs/heads/main","repository":{"full_name":"acme/control-tower-api"}}', TIMESTAMP '2026-04-18 08:30:00', 'PROCESSED', NULL, TIMESTAMP '2026-04-18 08:30:05'),
    ('outbox-cb-002', 'del-gh-002', 'workflow_run', '{"action":"completed","workflow_run":{"id":9001015}}', TIMESTAMP '2026-04-18 07:08:48', 'PROCESSED', NULL, TIMESTAMP '2026-04-18 07:08:52'),
    ('outbox-cb-003', 'del-gh-003', 'pull_request', '{"action":"opened","pull_request":{"number":101}}', TIMESTAMP '2026-04-08 10:30:00', 'PROCESSED', NULL, TIMESTAMP '2026-04-08 10:30:03'),
    ('outbox-cb-004', 'del-gh-004', 'check_suite', '{"action":"completed","check_suite":{"head_sha":"b2c3d4e5"}}', TIMESTAMP '2026-04-18 08:08:15', 'PROCESSED', NULL, TIMESTAMP '2026-04-18 08:08:18'),
    ('outbox-cb-005', 'del-gh-005', 'push', '{"ref":"refs/heads/feature/incremental-load","repository":{"full_name":"acme/data-pipeline"}}', TIMESTAMP '2026-04-17 20:00:00', 'FAILED', '{"code":"PARSE_ERROR","message":"Unexpected payload structure for push event"}', NULL);
