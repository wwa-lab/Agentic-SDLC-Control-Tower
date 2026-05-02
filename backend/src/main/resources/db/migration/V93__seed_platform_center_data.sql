INSERT INTO PLATFORM_SNOW_GROUP
    (id, servicenow_group_name, display_name, owner_email, escalation_policy, status, created_at, updated_at)
VALUES
    ('snow-fin-tech-ops', 'FIN-TECH-OPS', 'FIN-TECH-OPS', 'fin-tech-ops@example.com', 'business-hours-primary', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PLATFORM_APPLICATION
    (id, app_key, name, owner_snow_group_id, criticality, status, created_at, updated_at)
VALUES
    ('app-payment-gateway-pro', 'payment-gateway-pro', 'Payment-Gateway-Pro', 'snow-fin-tech-ops', 'tier-1', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PLATFORM_WORKSPACE
    (id, workspace_key, name, application_id, snow_group_id, status, created_at, updated_at)
VALUES
    ('ws-default-001', 'global-sdlc-tower', 'Global SDLC Tower', 'app-payment-gateway-pro', 'snow-fin-tech-ops', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PLATFORM_USER
    (staff_id, display_name, staff_name, avatar_url, email, profile_source, status, password_hash, created_at, updated_at)
VALUES
    ('43910516', 'Platform Admin', NULL, NULL, 'admin@sdlctower.local', 'manual', 'active', 'local-dev-placeholder-hash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('43910001', 'Backup Admin', NULL, NULL, 'backup-admin@sdlctower.local', 'manual', 'active', 'local-dev-placeholder-hash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('43910000', 'Alice', 'Alice Chen', 'https://teambook.company.com/avatar/43910000', 'alice@example.com', 'teambook', 'active', 'local-dev-placeholder-hash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PLATFORM_ROLE_ASSIGNMENT
    (id, staff_id, user_display_name, role, scope_type, scope_id, granted_by, granted_at)
VALUES
    ('ra-admin-platform', '43910516', 'Platform Admin', 'PLATFORM_ADMIN', 'platform', '*', 'system', CURRENT_TIMESTAMP),
    ('ra-backup-admin-platform', '43910001', 'Backup Admin', 'PLATFORM_ADMIN', 'platform', '*', 'system', CURRENT_TIMESTAMP),
    ('ra-alice-app', '43910000', 'Alice', 'WORKSPACE_MEMBER', 'application', 'app-payment-gateway-pro', '43910516', CURRENT_TIMESTAMP),
    ('ra-alice-snow', '43910000', 'Alice', 'WORKSPACE_VIEWER', 'snow_group', 'snow-fin-tech-ops', '43910516', CURRENT_TIMESTAMP);

INSERT INTO PLATFORM_TEMPLATE
    (id, template_key, kind, name, status, owner_id, current_version_id, created_at, updated_at)
VALUES
    ('tpl-page-control', 'control-tower-page', 'page', 'Control Tower Page', 'published', '43910516', 'tpl-page-control-v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PLATFORM_TEMPLATE_VERSION
    (id, template_id, version_number, body, created_at, created_by)
VALUES
    ('tpl-page-control-v1', 'tpl-page-control', 1, '{"layout":"catalog-detail"}', CURRENT_TIMESTAMP, '43910516');

INSERT INTO PLATFORM_CONFIGURATION
    (id, config_key, kind, scope_type, scope_id, parent_id, status, body, has_drift, last_modified_at)
VALUES
    ('cfg-nav-density', 'shell.nav.density', 'component', 'platform', '*', NULL, 'active', '{"density":"high"}', FALSE, CURRENT_TIMESTAMP),
    ('cfg-ai-default', 'ai.autonomy.default', 'ai-config', 'application', 'app-payment-gateway-pro', 'cfg-nav-density', 'active', '{"level":"L2"}', TRUE, CURRENT_TIMESTAMP);

INSERT INTO PLATFORM_AUDIT
    (id, event_time, actor, actor_type, category, action, object_type, object_id, scope_type, scope_id, outcome, evidence_ref, payload)
VALUES
    ('aud-0001', CURRENT_TIMESTAMP, 'system', 'system', 'permission_change', 'role.grant', 'role_assignment', 'ra-admin-platform', 'platform', '*', 'success', NULL, '{"seed":true}');

INSERT INTO PLATFORM_POLICY
    (id, policy_key, name, category, scope_type, scope_id, bound_to, version_number, status, body, created_at, created_by)
VALUES
    ('pol-release-approval', 'release-approval', 'Release Approval', 'approval', 'platform', '*', 'deploy.release', 1, 'active', '{"required":true}', CURRENT_TIMESTAMP, '43910516');

INSERT INTO PLATFORM_CREDENTIAL_REF
    (id, provider, external_ref, created_at)
VALUES
    ('cred-jira-demo', 'stub', NULL, CURRENT_TIMESTAMP),
    ('cred-confluence-demo', 'stub', NULL, CURRENT_TIMESTAMP);

INSERT INTO PLATFORM_CONNECTION
    (id, kind, scope_workspace_id, application_id, application_name, snow_group_id, snow_group_name, base_url, credential_ref, sync_mode, pull_schedule, push_url, status, last_sync_at, last_test_at, last_test_ok)
VALUES
    ('conn-jira-ws1', 'jira', 'ws-default-001', 'app-payment-gateway-pro', 'Payment-Gateway-Pro', 'snow-fin-tech-ops', 'FIN-TECH-OPS', 'https://jira.company.com', 'cred-jira-demo', 'both', '0 */15 * * * *', NULL, 'enabled', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);
