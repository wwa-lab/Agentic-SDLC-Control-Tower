INSERT INTO test_plan (
    id, workspace_id, project_id, name, description, owner_member_id, state, release_target, created_at, updated_at
) VALUES
    ('plan-auth-001', 'ws-default-001', 'proj-42', 'Gateway Authentication Regression', 'Regression pack for auth, session continuity, and audit trail coverage.', 'u-020', 'ACTIVE', '2026.04', TIMESTAMP WITH TIME ZONE '2026-04-15 08:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-18 03:15:00+00:00'),
    ('plan-rbac-001', 'ws-default-001', 'proj-11', 'Role Matrix Verification', 'Access-role validation pack for admin, reviewer, and viewer journeys.', 'u-020', 'ACTIVE', '2026.05', TIMESTAMP WITH TIME ZONE '2026-04-12 08:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 16:00:00+00:00'),
    ('plan-perf-001', 'ws-default-001', 'proj-55', 'Latency Guardrail Suite', 'Performance and soak coverage for the API reliability backlog.', 'u-021', 'ACTIVE', '2026.05', TIMESTAMP WITH TIME ZONE '2026-04-11 09:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-18 01:10:00+00:00'),
    ('plan-legacy-001', 'ws-default-001', 'proj-88', 'Legacy Exit Smoke Pack', 'Archived smoke coverage retained for audit reference only.', 'u-021', 'ARCHIVED', '2026.03', TIMESTAMP WITH TIME ZONE '2026-03-20 10:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-10 09:00:00+00:00');

INSERT INTO test_environment (
    id, workspace_id, project_id, name, description, kind, url, archived, created_at, updated_at
) VALUES
    ('env-stage-001', 'ws-default-001', 'proj-42', 'Shared Staging', 'Primary pre-release environment for regression execution.', 'STAGING', 'https://stage.control-tower.example', FALSE, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 00:00:00+00:00'),
    ('env-perf-001', 'ws-default-001', 'proj-55', 'Load Lab', 'Synthetic load environment without customer credentials.', 'EPHEMERAL', 'https://perf.control-tower.example', FALSE, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 00:00:00+00:00'),
    ('env-archive-001', 'ws-default-001', 'proj-88', 'Legacy Staging', 'Historical reference environment for archived runs.', 'OTHER', NULL, TRUE, TIMESTAMP WITH TIME ZONE '2026-02-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00');

INSERT INTO test_case (
    id, workspace_id, project_id, plan_id, title, type, priority, state, origin, owner_member_id, preconditions, steps, expected_result, created_at, updated_at
) VALUES
    ('case-auth-4201', 'ws-default-001', 'proj-42', 'plan-auth-001', 'Card payment happy path', 'FUNCTIONAL', 'P0', 'ACTIVE', 'MANUAL', 'u-020', '- User is authenticated\n- Checkout basket is valid', '1. Open checkout\n2. Submit card details\n3. Confirm payment', 'Payment completes and receipt is issued.', TIMESTAMP WITH TIME ZONE '2026-04-15 08:15:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:45:00+00:00'),
    ('case-auth-4202', 'ws-default-001', 'proj-42', 'plan-auth-001', '3DS timeout fallback', 'REGRESSION', 'P1', 'ACTIVE', 'MANUAL', 'u-020', '- Upstream ACS is slow', '1. Start payment\n2. Simulate 3DS timeout\n3. Observe fallback handling', 'Fallback flow records a recoverable error and exposes retry CTA.', TIMESTAMP WITH TIME ZONE '2026-04-15 08:20:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:45:00+00:00'),
    ('case-ai-4203', 'ws-default-001', 'proj-42', 'plan-auth-001', 'AI draft for audit timeline completeness', 'FUNCTIONAL', 'P2', 'DRAFT', 'AI_DRAFT', 'u-020', '- Audit trail events are enabled', '1. Trigger auth flow\n2. Review emitted events', 'Draft candidate checks audit completeness.', TIMESTAMP WITH TIME ZONE '2026-04-17 12:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 12:00:00+00:00'),
    ('case-rbac-1101', 'ws-default-001', 'proj-11', 'plan-rbac-001', 'Role matrix edit permissions', 'FUNCTIONAL', 'P1', 'ACTIVE', 'MANUAL', 'u-020', '- Admin account exists', '1. Open role matrix\n2. Edit reviewer permissions\n3. Save changes', 'Only allowed cells persist and the audit event is emitted.', TIMESTAMP WITH TIME ZONE '2026-04-12 08:15:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-12 08:15:00+00:00'),
    ('case-color-1102', 'ws-default-001', 'proj-11', 'plan-rbac-001', 'Requirement chip color matrix', 'SMOKE', 'P2', 'ACTIVE', 'MANUAL', 'u-021', '- Linked requirement lookup is reachable', '1. Open case detail\n2. Inspect linked REQ chips', 'GREEN, AMBER, RED, and GREY chip states are all visible.', TIMESTAMP WITH TIME ZONE '2026-04-12 08:25:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-12 08:25:00+00:00'),
    ('case-perf-5501', 'ws-default-001', 'proj-55', 'plan-perf-001', 'API latency under load', 'PERF', 'P0', 'ACTIVE', 'MANUAL', 'u-021', '- Load profile "checkout-burst" is ready', '1. Run 15 minute burst\n2. Capture p95 latency', 'p95 stays below 200ms.', TIMESTAMP WITH TIME ZONE '2026-04-11 09:10:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-18 01:10:00+00:00'),
    ('case-legacy-8801', 'ws-default-001', 'proj-88', 'plan-legacy-001', 'Legacy export smoke', 'SMOKE', 'P3', 'DEPRECATED', 'IMPORTED', 'u-021', '- Legacy report job exists', '1. Export legacy report', 'Historical export still completes.', TIMESTAMP WITH TIME ZONE '2026-03-20 10:10:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-10 09:00:00+00:00');

INSERT INTO test_case_req_link (
    case_id, req_id, link_status, created_at
) VALUES
    ('case-auth-4201', 'REQ-0001', 'VERIFIED', TIMESTAMP WITH TIME ZONE '2026-04-15 08:15:00+00:00'),
    ('case-auth-4201', 'REQ-0005', 'VERIFIED', TIMESTAMP WITH TIME ZONE '2026-04-15 08:15:00+00:00'),
    ('case-auth-4202', 'REQ-0001', 'VERIFIED', TIMESTAMP WITH TIME ZONE '2026-04-15 08:20:00+00:00'),
    ('case-ai-4203', 'REQ-0010', 'VERIFIED', TIMESTAMP WITH TIME ZONE '2026-04-17 12:00:00+00:00'),
    ('case-rbac-1101', 'REQ-0002', 'VERIFIED', TIMESTAMP WITH TIME ZONE '2026-04-12 08:15:00+00:00'),
    ('case-color-1102', 'REQ-0006', 'VERIFIED', TIMESTAMP WITH TIME ZONE '2026-04-12 08:25:00+00:00'),
    ('case-color-1102', 'REQ-0002', 'VERIFIED', TIMESTAMP WITH TIME ZONE '2026-04-12 08:25:00+00:00'),
    ('case-color-1102', 'REQ-4040', 'UNKNOWN_REQ', TIMESTAMP WITH TIME ZONE '2026-04-12 08:25:00+00:00'),
    ('case-color-1102', 'REQ-0010', 'UNVERIFIED', TIMESTAMP WITH TIME ZONE '2026-04-12 08:25:00+00:00'),
    ('case-perf-5501', 'REQ-0003', 'VERIFIED', TIMESTAMP WITH TIME ZONE '2026-04-11 09:10:00+00:00'),
    ('case-legacy-8801', 'REQ-0009', 'VERIFIED', TIMESTAMP WITH TIME ZONE '2026-03-20 10:10:00+00:00');

INSERT INTO test_run (
    id, workspace_id, project_id, plan_id, environment_id, state, trigger_source, actor_member_id, external_run_id, duration_sec, pass_count, fail_count, skip_count, error_count, started_at, completed_at
) VALUES
    ('run-auth-001', 'ws-default-001', 'proj-42', 'plan-auth-001', 'env-stage-001', 'FAILED', 'CI_WEBHOOK', 'u-020', 'gha-4201', 612, 1, 1, 0, 0, TIMESTAMP WITH TIME ZONE '2026-04-17 09:40:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:50:12+00:00'),
    ('run-rbac-001', 'ws-default-001', 'proj-11', 'plan-rbac-001', 'env-stage-001', 'PASSED', 'MANUAL_UPLOAD', 'u-020', 'manual-1101', 248, 1, 0, 0, 0, TIMESTAMP WITH TIME ZONE '2026-04-08 06:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-08 06:04:08+00:00'),
    ('run-perf-001', 'ws-default-001', 'proj-55', 'plan-perf-001', 'env-perf-001', 'FAILED', 'CI_WEBHOOK', 'u-021', 'gha-5501', 930, 0, 1, 0, 0, TIMESTAMP WITH TIME ZONE '2026-04-18 00:40:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-18 00:55:30+00:00');

INSERT INTO test_case_result (
    id, run_id, case_id, outcome, duration_sec, last_passed_at, created_at
) VALUES
    ('result-auth-4201', 'run-auth-001', 'case-auth-4201', 'PASS', 210, TIMESTAMP WITH TIME ZONE '2026-04-17 09:43:30+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:43:30+00:00'),
    ('result-auth-4202', 'run-auth-001', 'case-auth-4202', 'FAIL', 402, TIMESTAMP WITH TIME ZONE '2026-04-15 09:12:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:48:40+00:00'),
    ('result-rbac-1101', 'run-rbac-001', 'case-rbac-1101', 'PASS', 248, TIMESTAMP WITH TIME ZONE '2026-04-08 06:04:08+00:00', TIMESTAMP WITH TIME ZONE '2026-04-08 06:04:08+00:00'),
    ('result-perf-5501', 'run-perf-001', 'case-perf-5501', 'FAIL', 930, TIMESTAMP WITH TIME ZONE '2026-04-10 00:55:30+00:00', TIMESTAMP WITH TIME ZONE '2026-04-18 00:55:30+00:00');

INSERT INTO test_failure_summary (
    result_id, failure_excerpt, created_at
) VALUES
    ('result-auth-4202', REPEAT('3ds timeout while awaiting callback. bearer token already redacted. ', 90), TIMESTAMP WITH TIME ZONE '2026-04-17 09:48:40+00:00'),
    ('result-perf-5501', 'p95 latency breached threshold and sustained saturation for the full burst window.', TIMESTAMP WITH TIME ZONE '2026-04-18 00:55:30+00:00');

INSERT INTO testing_management_change_log (
    id, workspace_id, project_id, entry_type, reference_id, actor_member_id, payload_json, occurred_at
) VALUES
    ('tm-log-001', 'ws-default-001', 'proj-42', 'RUN_INGESTED', 'run-auth-001', 'u-020', '{"planId":"plan-auth-001","status":"FAILED"}', TIMESTAMP WITH TIME ZONE '2026-04-17 09:50:12+00:00'),
    ('tm-log-002', 'ws-default-001', 'proj-11', 'PLAN_REVIEWED', 'plan-rbac-001', 'u-020', '{"state":"ACTIVE"}', TIMESTAMP WITH TIME ZONE '2026-04-12 08:30:00+00:00');
