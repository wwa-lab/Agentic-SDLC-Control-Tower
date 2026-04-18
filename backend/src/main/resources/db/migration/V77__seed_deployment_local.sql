-- Jenkins Instance
INSERT INTO jenkins_instance (id, workspace_id, name, base_url) VALUES
('ji-local-001', 'ws-default-001', 'Local Jenkins', 'https://jenkins.example.com');

-- Applications
INSERT INTO dp_application (id, workspace_id, project_id, jenkins_instance_id, name, slug, runtime_label, jenkins_folder_path, description) VALUES
('app-order-svc', 'ws-default-001', 'proj-commerce', 'ji-local-001', 'Order Service', 'order-service', 'jvm', 'commerce/order-service', 'Core order processing microservice'),
('app-payment-gw', 'ws-default-001', 'proj-commerce', 'ji-local-001', 'Payment Gateway', 'payment-gateway', 'jvm', 'commerce/payment-gateway', 'PCI-compliant payment processing'),
('app-user-auth', 'ws-default-001', 'proj-identity', 'ji-local-001', 'User Auth', 'user-auth', 'jvm', 'identity/user-auth', 'OAuth2 + SAML identity provider');

-- Environments (4 per app)
INSERT INTO dp_application_environment (id, application_id, name, kind) VALUES
('env-os-dev', 'app-order-svc', 'dev', 'DEV'), ('env-os-qa', 'app-order-svc', 'qa', 'TEST'),
('env-os-stg', 'app-order-svc', 'staging', 'STAGING'), ('env-os-prod', 'app-order-svc', 'prod', 'PROD'),
('env-pg-dev', 'app-payment-gw', 'dev', 'DEV'), ('env-pg-qa', 'app-payment-gw', 'qa', 'TEST'),
('env-pg-stg', 'app-payment-gw', 'staging', 'STAGING'), ('env-pg-prod', 'app-payment-gw', 'prod', 'PROD'),
('env-ua-dev', 'app-user-auth', 'dev', 'DEV'), ('env-ua-qa', 'app-user-auth', 'qa', 'TEST'),
('env-ua-stg', 'app-user-auth', 'staging', 'STAGING'), ('env-ua-prod', 'app-user-auth', 'prod', 'PROD');

-- Releases (3 per app)
INSERT INTO dp_release (id, application_id, release_version, build_artifact_slice_id, build_artifact_id, state, created_at, jenkins_source_url) VALUES
('release-os-042', 'app-order-svc', '2026.04.17-0042', 'code-build', 'ba-042', 'DEPLOYED', TIMESTAMP '2026-04-17 08:00:00', 'https://jenkins.example.com/job/commerce/job/order-service/42/'),
('release-os-038', 'app-order-svc', '2026.04.15-0038', 'code-build', 'ba-038', 'SUPERSEDED', TIMESTAMP '2026-04-15 08:00:00', 'https://jenkins.example.com/job/commerce/job/order-service/38/'),
('release-os-043', 'app-order-svc', '2026.04.18-0043', 'code-build', 'ba-043', 'PREPARED', TIMESTAMP '2026-04-18 06:00:00', 'https://jenkins.example.com/job/commerce/job/order-service/43/'),
('release-pg-015', 'app-payment-gw', '2026.04.17-0015', 'code-build', 'ba-pg-015', 'DEPLOYED', TIMESTAMP '2026-04-17 07:00:00', 'https://jenkins.example.com/job/commerce/job/payment-gateway/15/'),
('release-pg-012', 'app-payment-gw', '2026.04.14-0012', 'code-build', 'ba-pg-012', 'SUPERSEDED', TIMESTAMP '2026-04-14 07:00:00', 'https://jenkins.example.com/job/commerce/job/payment-gateway/12/'),
('release-pg-016', 'app-payment-gw', '2026.04.18-0016', 'code-build', 'ba-pg-016', 'PREPARED', TIMESTAMP '2026-04-18 05:00:00', 'https://jenkins.example.com/job/commerce/job/payment-gateway/16/'),
('release-ua-021', 'app-user-auth', '2026.04.17-0021', 'code-build', 'ba-ua-021', 'DEPLOYED', TIMESTAMP '2026-04-17 06:00:00', 'https://jenkins.example.com/job/identity/job/user-auth/21/'),
('release-ua-018', 'app-user-auth', '2026.04.14-0018', 'code-build', 'ba-ua-018', 'SUPERSEDED', TIMESTAMP '2026-04-14 06:00:00', 'https://jenkins.example.com/job/identity/job/user-auth/18/'),
('release-ua-022', 'app-user-auth', '2026.04.18-0022', 'code-build', 'ba-ua-022', 'PREPARED', TIMESTAMP '2026-04-18 04:00:00', 'https://jenkins.example.com/job/identity/job/user-auth/22/');

-- Deploys (10 for order-svc, varied states)
INSERT INTO dp_deploy (id, application_id, release_id, environment_name, jenkins_instance_id, jenkins_job_full_name, jenkins_build_number, jenkins_build_url, trigger, actor, state, started_at, completed_at, duration_sec, is_rollback, last_ingested_at) VALUES
('deploy-os-001', 'app-order-svc', 'release-os-042', 'dev', 'ji-local-001', 'commerce/order-service/dev-deploy', 142, 'https://jenkins.example.com/142/', 'PUSH_TO_MAIN', 'alice', 'SUCCEEDED', TIMESTAMP '2026-04-17 09:00:00', TIMESTAMP '2026-04-17 09:02:00', 120, FALSE, TIMESTAMP '2026-04-17 09:02:05'),
('deploy-os-002', 'app-order-svc', 'release-os-042', 'qa', 'ji-local-001', 'commerce/order-service/qa-deploy', 143, 'https://jenkins.example.com/143/', 'PROMOTE_FROM_DEV', 'alice', 'SUCCEEDED', TIMESTAMP '2026-04-17 10:00:00', TIMESTAMP '2026-04-17 10:03:00', 180, FALSE, TIMESTAMP '2026-04-17 10:03:05'),
('deploy-os-003', 'app-order-svc', 'release-os-042', 'staging', 'ji-local-001', 'commerce/order-service/staging-deploy', 144, 'https://jenkins.example.com/144/', 'PROMOTE_FROM_TEST', 'bob', 'SUCCEEDED', TIMESTAMP '2026-04-17 12:00:00', TIMESTAMP '2026-04-17 12:04:00', 240, FALSE, TIMESTAMP '2026-04-17 12:04:05'),
('deploy-os-004', 'app-order-svc', 'release-os-042', 'prod', 'ji-local-001', 'commerce/order-service/prod-deploy', 145, 'https://jenkins.example.com/145/', 'MANUAL', 'jane.pm', 'SUCCEEDED', TIMESTAMP '2026-04-17 14:00:00', TIMESTAMP '2026-04-17 14:05:00', 300, FALSE, TIMESTAMP '2026-04-17 14:05:05'),
('deploy-os-005', 'app-order-svc', 'release-os-038', 'prod', 'ji-local-001', 'commerce/order-service/prod-deploy', 140, 'https://jenkins.example.com/140/', 'MANUAL', 'jane.pm', 'SUCCEEDED', TIMESTAMP '2026-04-15 10:00:00', TIMESTAMP '2026-04-15 10:04:00', 240, FALSE, TIMESTAMP '2026-04-15 10:04:05'),
('deploy-os-006', 'app-order-svc', 'release-os-043', 'dev', 'ji-local-001', 'commerce/order-service/dev-deploy', 146, 'https://jenkins.example.com/146/', 'PUSH_TO_MAIN', 'carol', 'IN_PROGRESS', TIMESTAMP '2026-04-18 07:00:00', NULL, NULL, FALSE, TIMESTAMP '2026-04-18 07:00:05'),
('deploy-pg-001', 'app-payment-gw', 'release-pg-015', 'dev', 'ji-local-001', 'commerce/payment-gateway/dev-deploy', 50, 'https://jenkins.example.com/pg/50/', 'PUSH_TO_MAIN', 'dave', 'SUCCEEDED', TIMESTAMP '2026-04-17 08:00:00', TIMESTAMP '2026-04-17 08:01:30', 90, FALSE, TIMESTAMP '2026-04-17 08:01:35'),
('deploy-pg-002', 'app-payment-gw', 'release-pg-015', 'staging', 'ji-local-001', 'commerce/payment-gateway/staging-deploy', 51, 'https://jenkins.example.com/pg/51/', 'PROMOTE_FROM_TEST', 'dave', 'FAILED', TIMESTAMP '2026-04-17 11:00:00', TIMESTAMP '2026-04-17 11:03:00', 180, FALSE, TIMESTAMP '2026-04-17 11:03:05'),
('deploy-ua-001', 'app-user-auth', 'release-ua-021', 'prod', 'ji-local-001', 'identity/user-auth/prod-deploy', 80, 'https://jenkins.example.com/ua/80/', 'MANUAL', 'jane.pm', 'SUCCEEDED', TIMESTAMP '2026-04-17 15:00:00', TIMESTAMP '2026-04-17 15:05:00', 300, FALSE, TIMESTAMP '2026-04-17 15:05:05'),
('deploy-ua-002', 'app-user-auth', 'release-ua-018', 'prod', 'ji-local-001', 'identity/user-auth/prod-deploy', 75, 'https://jenkins.example.com/ua/75/', 'ROLLBACK', 'bob', 'ROLLED_BACK', TIMESTAMP '2026-04-14 18:00:00', TIMESTAMP '2026-04-14 18:04:00', 240, TRUE, TIMESTAMP '2026-04-14 18:04:05');

-- Deploy Stages (for deploy-os-004 prod)
INSERT INTO dp_deploy_stage (id, deploy_id, name, stage_order, state, started_at, completed_at, duration_sec) VALUES
('stg-os4-1', 'deploy-os-004', 'Checkout', 1, 'SUCCESS', TIMESTAMP '2026-04-17 14:00:00', TIMESTAMP '2026-04-17 14:00:15', 15),
('stg-os4-2', 'deploy-os-004', 'Build', 2, 'SUCCESS', TIMESTAMP '2026-04-17 14:00:15', TIMESTAMP '2026-04-17 14:01:30', 75),
('stg-os4-3', 'deploy-os-004', 'Unit Tests', 3, 'SUCCESS', TIMESTAMP '2026-04-17 14:01:30', TIMESTAMP '2026-04-17 14:03:00', 90),
('stg-os4-4', 'deploy-os-004', 'Deploy to Prod', 4, 'SUCCESS', TIMESTAMP '2026-04-17 14:03:00', TIMESTAMP '2026-04-17 14:04:00', 60),
('stg-os4-5', 'deploy-os-004', 'Smoke Tests', 5, 'SUCCESS', TIMESTAMP '2026-04-17 14:04:00', TIMESTAMP '2026-04-17 14:05:00', 60);

-- Approval Events
INSERT INTO dp_approval_event (id, deploy_id, stage_id, stage_name, approver_member_id, approver_display_name, approver_role, decision, gate_policy_version, rationale_cipher, decided_at) VALUES
('appr-os-001', 'deploy-os-004', 'stg-os4-4', 'Deploy to Prod', 'member-jane', 'Jane PM', 'PM', 'APPROVED', 'gate-v1', 'Release is clean — all QA signoff complete.', TIMESTAMP '2026-04-17 14:02:50'),
('appr-ua-001', 'deploy-ua-002', NULL, 'Emergency Rollback', 'member-bob', 'Bob TechLead', 'TECH_LEAD', 'APPROVED', 'gate-v1', 'Emergency rollback — prod regression on auth tokens.', TIMESTAMP '2026-04-14 18:00:30');

-- AI Release Notes
INSERT INTO dp_ai_release_notes (id, release_id, skill_version, status, body_markdown, risk_hint, generated_at) VALUES
('ain-os-001', 'release-os-042', 'release-notes-v3', 'SUCCESS', '## Release 2026.04.17-0042\n\nThis release includes 3 bug fixes and 1 new feature.', 'LOW', TIMESTAMP '2026-04-17 09:00:00'),
('ain-pg-001', 'release-pg-015', 'release-notes-v3', 'SUCCESS', '## Release 2026.04.17-0015\n\nPayment SDK update and retry logic improvements.', 'MEDIUM', TIMESTAMP '2026-04-17 08:00:00'),
('ain-ua-001', 'release-ua-021', 'release-notes-v3', 'SUCCESS', '## Release 2026.04.17-0021\n\nToken refresh and SSO session handling improvements.', 'LOW', TIMESTAMP '2026-04-17 07:00:00');

-- AI Deployment Summaries
INSERT INTO dp_ai_deployment_summary (id, deploy_id, skill_version, status, narrative, generated_at) VALUES
('aids-os-001', 'deploy-os-004', 'deploy-summary-v2', 'SUCCESS', 'Prod deploy of Order Service v2026.04.17-0042 completed successfully in 5m.', TIMESTAMP '2026-04-17 14:06:00'),
('aids-pg-001', 'deploy-pg-002', 'deploy-summary-v2', 'FAILED', NULL, TIMESTAMP '2026-04-17 11:04:00'),
('aids-ua-001', 'deploy-ua-001', 'deploy-summary-v2', 'SUCCESS', 'User Auth v2026.04.17-0021 deployed to prod. Token refresh regression resolved.', TIMESTAMP '2026-04-17 15:06:00');
