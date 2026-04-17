INSERT INTO milestones (
    id, project_id, label, target_date, status, percent_complete, owner_member_id,
    slippage_reason, is_current, ordering, created_at, updated_at
) VALUES
    ('MS-PROJ42-01', 'proj-42', 'Discovery Complete', DATE '2026-03-15', 'COMPLETED', 100, 'u-007', NULL, FALSE, 1, TIMESTAMP WITH TIME ZONE '2026-03-15 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-03-15 00:00:00+00:00'),
    ('MS-PROJ42-02', 'proj-42', 'Alpha Release', DATE '2026-05-01', 'IN_PROGRESS', 60, 'u-007', NULL, TRUE, 2, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 10:05:00+00:00'),
    ('MS-PROJ42-03', 'proj-42', 'GA Launch', DATE '2026-06-30', 'AT_RISK', 20, 'u-007', 'Upstream identity-service-v2 slipped one sprint', FALSE, 3, TIMESTAMP WITH TIME ZONE '2026-04-02 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:30:00+00:00'),
    ('MS-PROJ11-01', 'proj-11', 'Discovery Complete', DATE '2026-03-15', 'COMPLETED', 100, 'u-007', NULL, FALSE, 1, TIMESTAMP WITH TIME ZONE '2026-03-15 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-03-15 00:00:00+00:00'),
    ('MS-PROJ11-02', 'proj-11', 'Alpha Release', DATE '2026-05-01', 'IN_PROGRESS', 60, 'u-007', NULL, TRUE, 2, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('MS-PROJ55-01', 'proj-55', 'Discovery Complete', DATE '2026-03-15', 'COMPLETED', 100, 'u-007', NULL, FALSE, 1, TIMESTAMP WITH TIME ZONE '2026-03-15 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-03-15 00:00:00+00:00'),
    ('MS-PROJ55-02', 'proj-55', 'Alpha Release', DATE '2026-05-01', 'IN_PROGRESS', 60, 'u-007', NULL, TRUE, 2, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:15:00+00:00'),
    ('MS-PROJ55-03', 'proj-55', 'GA Launch', DATE '2026-06-30', 'AT_RISK', 20, 'u-007', 'Discovery scope depends on governance exception', FALSE, 3, TIMESTAMP WITH TIME ZONE '2026-04-02 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 07:30:00+00:00'),
    ('MS-PROJ88-01', 'proj-88', 'Discovery Complete', DATE '2026-03-15', 'COMPLETED', 100, 'u-007', NULL, FALSE, 1, TIMESTAMP WITH TIME ZONE '2026-03-15 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-03-15 00:00:00+00:00'),
    ('MS-PROJ88-02', 'proj-88', 'Alpha Release', DATE '2026-04-24', 'AT_RISK', 42, 'u-007', 'Rollback automation remains unstable', TRUE, 2, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 10:05:00+00:00'),
    ('MS-PROJ88-03', 'proj-88', 'Cutover Completion', DATE '2026-05-28', 'AT_RISK', 20, 'u-007', 'Cutover is blocked until rollback confidence recovers', FALSE, 3, TIMESTAMP WITH TIME ZONE '2026-04-02 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:00:00+00:00');

INSERT INTO project_dependencies (
    id, source_project_id, target_name, target_ref, target_project_id, direction, relationship,
    owner_team, health, blocker_reason, external, created_at, updated_at
) VALUES
    ('DEP-P42-UP-1', 'proj-42', 'Identity-Service-V2', 'proj-identity-v2', NULL, 'UPSTREAM', 'API', 'Identity Platform', 'YELLOW', 'Token exchange API is slipping one sprint', TRUE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('DEP-P42-UP-2', 'proj-42', 'Compliance Evidence Pipeline', 'proj-11', 'proj-11', 'UPSTREAM', 'DATA', 'Governance Ops', 'GREEN', NULL, FALSE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('DEP-P42-DOWN-1', 'proj-42', 'Customer Checkout Runtime', 'runtime-checkout', NULL, 'DOWNSTREAM', 'SLA', 'Checkout Experience', 'YELLOW', 'Release window is tightly coupled', TRUE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('DEP-P42-DOWN-2', 'proj-42', 'Fraud Detection Expansion', 'proj-55', 'proj-55', 'DOWNSTREAM', 'SCHEDULE', 'Fraud Platform', 'YELLOW', NULL, FALSE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('DEP-P11-UP-1', 'proj-11', 'Identity-Service-V2', 'proj-identity-v2', NULL, 'UPSTREAM', 'API', 'Identity Platform', 'GREEN', NULL, TRUE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('DEP-P11-DOWN-1', 'proj-11', 'Fraud Detection Expansion', 'proj-55', 'proj-55', 'DOWNSTREAM', 'SCHEDULE', 'Fraud Platform', 'GREEN', NULL, FALSE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('DEP-P55-UP-1', 'proj-55', 'Identity-Service-V2', 'proj-identity-v2', NULL, 'UPSTREAM', 'API', 'Identity Platform', 'YELLOW', 'Partner design review is pending', TRUE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('DEP-P55-DOWN-1', 'proj-55', 'Gateway Migration', 'proj-42', 'proj-42', 'DOWNSTREAM', 'SCHEDULE', 'Delivery Ops', 'YELLOW', NULL, FALSE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('DEP-P88-UP-1', 'proj-88', 'Identity-Service-V2', 'proj-identity-v2', NULL, 'UPSTREAM', 'API', 'Identity Platform', 'YELLOW', 'Rollback certification is blocked on partner API parity', TRUE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00'),
    ('DEP-P88-DOWN-1', 'proj-88', 'Customer Checkout Runtime', 'runtime-checkout', NULL, 'DOWNSTREAM', 'SLA', 'Checkout Experience', 'RED', 'Rollback window exceeds support SLA', TRUE, TIMESTAMP WITH TIME ZONE '2026-04-10 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:00:00+00:00');

INSERT INTO environments (
    id, project_id, label, kind, gate_status, approver_member_id, created_at, updated_at
) VALUES
    ('ENV-P42-DEV', 'proj-42', 'DEV', 'DEV', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:15:00+00:00'),
    ('ENV-P42-STAGE', 'proj-42', 'STAGING', 'STAGING', 'APPROVAL_REQUIRED', 'u-003', TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00'),
    ('ENV-P42-PROD', 'proj-42', 'PROD', 'PROD', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 23:50:00+00:00'),
    ('ENV-P11-DEV', 'proj-11', 'DEV', 'DEV', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:15:00+00:00'),
    ('ENV-P11-STAGE', 'proj-11', 'STAGING', 'STAGING', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00'),
    ('ENV-P11-PROD', 'proj-11', 'PROD', 'PROD', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 23:50:00+00:00'),
    ('ENV-P55-DEV', 'proj-55', 'DEV', 'DEV', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:15:00+00:00'),
    ('ENV-P55-STAGE', 'proj-55', 'STAGING', 'STAGING', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00'),
    ('ENV-P55-PROD', 'proj-55', 'PROD', 'PROD', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 23:50:00+00:00'),
    ('ENV-P88-DEV', 'proj-88', 'DEV', 'DEV', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:15:00+00:00'),
    ('ENV-P88-STAGE', 'proj-88', 'STAGING', 'STAGING', 'AUTO', NULL, TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00'),
    ('ENV-P88-PROD', 'proj-88', 'PROD', 'PROD', 'BLOCKED', 'u-030', TIMESTAMP WITH TIME ZONE '2026-04-01 00:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 23:50:00+00:00');

INSERT INTO deployments (
    id, environment_id, version_ref, build_id, health, deployed_at, commit_distance_from_prod
) VALUES
    ('DEPLOY-P42-DEV', 'ENV-P42-DEV', 'v2.4.0-rc.6', 'build-1042', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-17 08:15:00+00:00', 0),
    ('DEPLOY-P42-STAGE', 'ENV-P42-STAGE', 'v2.4.0-rc.4', 'build-1035', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00', 6),
    ('DEPLOY-P42-PROD', 'ENV-P42-PROD', 'v2.4.0', 'build-1028', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-16 23:50:00+00:00', 0),
    ('DEPLOY-P11-DEV', 'ENV-P11-DEV', 'v2.4.0-rc.2', 'build-980', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-17 08:15:00+00:00', 0),
    ('DEPLOY-P11-STAGE', 'ENV-P11-STAGE', 'v2.4.0-rc.1', 'build-972', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00', 3),
    ('DEPLOY-P11-PROD', 'ENV-P11-PROD', 'v2.4.0', 'build-968', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-16 23:50:00+00:00', 0),
    ('DEPLOY-P55-DEV', 'ENV-P55-DEV', 'v2.5.0-alpha.2', 'build-880', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-17 08:15:00+00:00', 0),
    ('DEPLOY-P55-STAGE', 'ENV-P55-STAGE', 'v2.5.0-alpha.1', 'build-871', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00', 4),
    ('DEPLOY-P55-PROD', 'ENV-P55-PROD', 'v2.4.8', 'build-860', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-16 23:50:00+00:00', 0),
    ('DEPLOY-P88-DEV', 'ENV-P88-DEV', 'v2.3.1-hotfix.5', 'build-1002', 'GREEN', TIMESTAMP WITH TIME ZONE '2026-04-17 08:15:00+00:00', 0),
    ('DEPLOY-P88-STAGE', 'ENV-P88-STAGE', 'v2.3.1-hotfix.3', 'build-998', 'YELLOW', TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00', 18),
    ('DEPLOY-P88-PROD', 'ENV-P88-PROD', 'v2.3.1', 'build-975', 'RED', TIMESTAMP WITH TIME ZONE '2026-04-16 23:50:00+00:00', 0);

INSERT INTO risk_signals (
    id, workspace_id, project_id, category, severity, source_kind, source_id, title, detail,
    action_label, action_url, skill_name, execution_id, detected_at, resolved_at
) VALUES
    ('PRISK-4201', 'ws-default-001', 'proj-42', 'DEPENDENCY', 'HIGH', 'PROJECT', 'proj-42', 'Identity dependency runway under 10 days', 'Partner team moved completion to Sprint 19', 'Review dependency lane', '/project-space/proj-42', 'risk-registry-audit', 'skill-run-proj-42', TIMESTAMP WITH TIME ZONE '2026-04-13 10:00:00+00:00', NULL),
    ('PRISK-4202', 'ws-default-001', 'proj-42', 'GOVERNANCE', 'MEDIUM', 'SPEC', 'SPEC-0088', 'Approval backlog > 72h', 'Architecture sign-off still pending', 'Review approvals', '/platform?view=approvals&projectId=proj-42', NULL, NULL, TIMESTAMP WITH TIME ZONE '2026-04-14 10:00:00+00:00', NULL),
    ('PRISK-5501', 'ws-default-001', 'proj-55', 'DELIVERY', 'HIGH', 'PROJECT', 'proj-55', 'Discovery scope is blocked on governance exception', 'Scope expansion needs policy exception review', 'Open Project Space', '/project-space/proj-55', 'risk-registry-audit', 'skill-run-proj-55', TIMESTAMP WITH TIME ZONE '2026-04-15 10:00:00+00:00', NULL),
    ('PRISK-5502', 'ws-default-001', 'proj-55', 'GOVERNANCE', 'MEDIUM', 'SPEC', 'SPEC-030', 'Approval backlog > 72h', 'Architecture sign-off still pending', 'Review approvals', '/platform?view=approvals&projectId=proj-55', NULL, NULL, TIMESTAMP WITH TIME ZONE '2026-04-14 10:00:00+00:00', NULL),
    ('PRISK-8801', 'ws-default-001', 'proj-88', 'DELIVERY', 'CRITICAL', 'INCIDENT', 'INC-0422', 'Rollback automation repeatedly fails smoke validation', 'Nightly rollback drill failed in STAGING', 'Open Incident', '/incidents/INC-0422', 'risk-registry-audit', 'skill-run-proj-88', TIMESTAMP WITH TIME ZONE '2026-04-08 10:00:00+00:00', NULL),
    ('PRISK-8802', 'ws-default-001', 'proj-88', 'DEPENDENCY', 'HIGH', 'PROJECT', 'proj-88', 'Support SLA breach risk remains active', 'Checkout runtime depends on rollback completion before cutover', 'Open Incident', '/incidents/INC-0422', NULL, NULL, TIMESTAMP WITH TIME ZONE '2026-04-12 10:00:00+00:00', NULL);
