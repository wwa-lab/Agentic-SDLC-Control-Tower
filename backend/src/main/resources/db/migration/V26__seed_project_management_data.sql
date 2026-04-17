INSERT INTO project_plan_revisions (project_id, current_revision) VALUES
    ('proj-07', 1),
    ('proj-11', 2),
    ('proj-42', 4),
    ('proj-55', 3),
    ('proj-88', 5),
    ('proj-private-01', 1),
    ('proj-degraded-001', 1);

UPDATE milestones SET
    description = 'Discovery and scope framing',
    pm_state = 'COMPLETED',
    plan_revision_at_update = 2
WHERE id = 'MS-PROJ42-01';

UPDATE milestones SET
    description = 'Alpha release readiness across service boundaries',
    pm_state = 'IN_PROGRESS',
    plan_revision_at_update = 4
WHERE id = 'MS-PROJ42-02';

UPDATE milestones SET
    description = 'General availability launch window',
    pm_state = 'AT_RISK',
    slippage_risk_score = 'HIGH',
    slippage_risk_factors = 'Dependency blocked 7 days',
    plan_revision_at_update = 4
WHERE id = 'MS-PROJ42-03';

UPDATE milestones SET
    description = 'Discovery complete',
    pm_state = 'COMPLETED',
    plan_revision_at_update = 2
WHERE id = 'MS-PROJ11-01';

UPDATE milestones SET
    description = 'Alpha release',
    pm_state = 'IN_PROGRESS',
    plan_revision_at_update = 2
WHERE id = 'MS-PROJ11-02';

UPDATE milestones SET
    description = 'Discovery complete',
    pm_state = 'COMPLETED',
    plan_revision_at_update = 3
WHERE id = 'MS-PROJ55-01';

UPDATE milestones SET
    description = 'Alpha release',
    pm_state = 'IN_PROGRESS',
    plan_revision_at_update = 3
WHERE id = 'MS-PROJ55-02';

UPDATE milestones SET
    description = 'General availability launch',
    pm_state = 'AT_RISK',
    slippage_risk_score = 'MEDIUM',
    slippage_risk_factors = 'Governance exception still pending',
    plan_revision_at_update = 3
WHERE id = 'MS-PROJ55-03';

UPDATE milestones SET
    description = 'Discovery complete',
    pm_state = 'COMPLETED',
    plan_revision_at_update = 5
WHERE id = 'MS-PROJ88-01';

UPDATE milestones SET
    description = 'Rollback stabilization checkpoint',
    pm_state = 'AT_RISK',
    slippage_risk_score = 'HIGH',
    slippage_risk_factors = 'Rollback drill failed twice this week',
    plan_revision_at_update = 5
WHERE id = 'MS-PROJ88-02';

UPDATE milestones SET
    description = 'Cutover completion',
    pm_state = 'SLIPPED',
    slippage_risk_score = 'HIGH',
    slippage_risk_factors = 'Cutover blocked until rollback confidence recovers',
    plan_revision_at_update = 5
WHERE id = 'MS-PROJ88-03';

UPDATE risk_signals SET
    pm_state = 'MITIGATING',
    owner_member_id = 'u-011',
    mitigation_note = 'Paired with data team Wednesday; contract review Thursday.',
    plan_revision_at_update = 4
WHERE id = 'PRISK-4201';

UPDATE risk_signals SET
    pm_state = 'IDENTIFIED',
    owner_member_id = 'u-007',
    plan_revision_at_update = 4
WHERE id = 'PRISK-4202';

UPDATE risk_signals SET
    pm_state = 'ACKNOWLEDGED',
    owner_member_id = 'u-011',
    mitigation_note = 'Draft exception packet underway for Friday review.',
    plan_revision_at_update = 3
WHERE id = 'PRISK-5501';

UPDATE risk_signals SET
    pm_state = 'IDENTIFIED',
    owner_member_id = 'u-007',
    plan_revision_at_update = 3
WHERE id = 'PRISK-5502';

UPDATE risk_signals SET
    pm_state = 'ESCALATED',
    owner_member_id = 'u-030',
    escalated_incident_id = 'INC-0422',
    plan_revision_at_update = 5
WHERE id = 'PRISK-8801';

UPDATE risk_signals SET
    pm_state = 'MITIGATING',
    owner_member_id = 'u-011',
    mitigation_note = 'Fallback checklist added to nightly rollback drill.',
    plan_revision_at_update = 5
WHERE id = 'PRISK-8802';

UPDATE project_dependencies SET
    pm_state = 'NEGOTIATING',
    plan_revision_at_update = 4
WHERE id = 'DEP-P42-UP-1';

UPDATE project_dependencies SET
    pm_state = 'APPROVED',
    counter_signed_by = 'u-007',
    counter_signed_at = TIMESTAMP WITH TIME ZONE '2026-04-16 10:00:00+00:00',
    plan_revision_at_update = 4
WHERE id = 'DEP-P42-UP-2';

UPDATE project_dependencies SET
    pm_state = 'AT_RISK',
    plan_revision_at_update = 4
WHERE id = 'DEP-P42-DOWN-1';

UPDATE project_dependencies SET
    pm_state = 'NEGOTIATING',
    plan_revision_at_update = 4
WHERE id = 'DEP-P42-DOWN-2';

UPDATE project_dependencies SET
    pm_state = 'APPROVED',
    contract_commitment = 'Partner confirmed delivery in sprint 19 via working agreement.',
    plan_revision_at_update = 2
WHERE id = 'DEP-P11-UP-1';

UPDATE project_dependencies SET
    pm_state = 'PROPOSED',
    plan_revision_at_update = 2
WHERE id = 'DEP-P11-DOWN-1';

UPDATE project_dependencies SET
    pm_state = 'NEGOTIATING',
    plan_revision_at_update = 3
WHERE id = 'DEP-P55-UP-1';

UPDATE project_dependencies SET
    pm_state = 'PROPOSED',
    plan_revision_at_update = 3
WHERE id = 'DEP-P55-DOWN-1';

UPDATE project_dependencies SET
    pm_state = 'NEGOTIATING',
    plan_revision_at_update = 5
WHERE id = 'DEP-P88-UP-1';

UPDATE project_dependencies SET
    pm_state = 'AT_RISK',
    plan_revision_at_update = 5
WHERE id = 'DEP-P88-DOWN-1';

INSERT INTO capacity_allocations (
    id, project_id, member_id, milestone_id, allocation_percent, justification,
    window_start, window_end, plan_revision, created_at, updated_at
) VALUES
    ('cap-p42-01', 'proj-42', 'u-011', 'MS-PROJ42-02', 60, NULL, DATE '2026-04-17', DATE '2026-05-15', 4, TIMESTAMP WITH TIME ZONE '2026-04-17 08:30:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:30:00+00:00'),
    ('cap-p42-02', 'proj-42', 'u-007', 'MS-PROJ42-03', 80, 'PM doubles as engineer this sprint', DATE '2026-04-17', DATE '2026-05-15', 4, TIMESTAMP WITH TIME ZONE '2026-04-17 08:30:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:30:00+00:00'),
    ('cap-p42-03', 'proj-42', 'u-015', 'MS-PROJ42-03', 30, NULL, DATE '2026-04-17', DATE '2026-05-15', 4, TIMESTAMP WITH TIME ZONE '2026-04-17 08:30:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:30:00+00:00'),
    ('cap-p55-01', 'proj-55', 'u-011', 'MS-PROJ55-03', 55, 'Critical partner review support', DATE '2026-04-17', DATE '2026-05-08', 3, TIMESTAMP WITH TIME ZONE '2026-04-17 07:30:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 07:30:00+00:00'),
    ('cap-p88-01', 'proj-88', 'u-030', 'MS-PROJ88-03', 110, 'Rollback hardening sprint', DATE '2026-04-17', DATE '2026-05-30', 5, TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 06:45:00+00:00');

INSERT INTO ai_suggestions (
    id, project_id, kind, target_type, target_id, payload_json, confidence, state,
    skill_execution_id, suppress_until, created_at, updated_at, resolved_at
) VALUES
    ('sug-p42-m1', 'proj-42', 'SLIPPAGE', 'MILESTONE', 'MS-PROJ42-03', '{"summary":"Move GA Launch into at-risk handling because upstream identity-service-v2 slipped by one sprint."}', 0.82, 'PENDING', 'skill-exec-pm-1', NULL, TIMESTAMP WITH TIME ZONE '2026-04-17 06:15:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 06:15:00+00:00', NULL),
    ('sug-p55-r1', 'proj-55', 'MITIGATION', 'RISK', 'PRISK-5501', '{"summary":"Pair with governance this week and prepare exception packet before Friday review."}', 0.77, 'PENDING', 'skill-exec-pm-2', NULL, TIMESTAMP WITH TIME ZONE '2026-04-17 06:20:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 06:20:00+00:00', NULL),
    ('sug-p88-d1', 'proj-88', 'DEP_RESOLUTION', 'DEPENDENCY', 'DEP-P88-DOWN-1', '{"summary":"Open negotiation with Checkout Experience on rollback SLA and intermediate signoff."}', 0.74, 'PENDING', 'skill-exec-pm-3', NULL, TIMESTAMP WITH TIME ZONE '2026-04-17 06:25:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 06:25:00+00:00', NULL),
    ('sug-p42-r-accepted', 'proj-42', 'MITIGATION', 'RISK', 'PRISK-4201', '{"summary":"Historic suggestion already accepted."}', 0.61, 'ACCEPTED', 'skill-exec-pm-4', NULL, TIMESTAMP WITH TIME ZONE '2026-04-16 06:25:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 08:25:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 08:25:00+00:00'),
    ('sug-p88-m-dismissed', 'proj-88', 'SLIPPAGE', 'MILESTONE', 'MS-PROJ88-02', '{"summary":"Historic suggestion dismissed due to stale context."}', 0.58, 'DISMISSED', 'skill-exec-pm-5', TIMESTAMP WITH TIME ZONE '2026-04-18 09:15:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 05:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:15:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 09:15:00+00:00');

UPDATE milestones SET ai_suggestion_id = 'sug-p42-m1' WHERE id = 'MS-PROJ42-03';

INSERT INTO plan_change_log_entries (
    id, project_id, actor_type, actor_member_id, skill_execution_id, action, target_type, target_id,
    before_json, after_json, reason, correlation_id, audit_link_id, created_at
) VALUES
    ('log-pm-001', 'proj-42', 'HUMAN', 'u-007', NULL, 'TRANSITION', 'MILESTONE', 'MS-PROJ42-03', '{"status":"IN_PROGRESS"}', '{"status":"AT_RISK"}', 'Upstream schema dependency slipped', 'corr-pm-001', 'audit-pm-001', TIMESTAMP WITH TIME ZONE '2026-04-17 08:45:00+00:00'),
    ('log-pm-002', 'proj-42', 'HUMAN', 'u-011', NULL, 'UPDATE', 'CAPACITY_ALLOCATION', 'proj-42', '{"row":"u-007"}', '{"row":"u-007","total":80}', 'PM doubles as engineer this sprint', 'corr-pm-002', 'audit-pm-002', TIMESTAMP WITH TIME ZONE '2026-04-17 08:50:00+00:00'),
    ('log-pm-003', 'proj-55', 'HUMAN', 'u-011', NULL, 'TRANSITION', 'RISK', 'PRISK-5501', '{"state":"IDENTIFIED"}', '{"state":"ACKNOWLEDGED"}', 'Exception packet drafted', 'corr-pm-003', 'audit-pm-003', TIMESTAMP WITH TIME ZONE '2026-04-17 08:20:00+00:00'),
    ('log-pm-004', 'proj-88', 'HUMAN', 'u-030', NULL, 'ESCALATE', 'RISK', 'PRISK-8801', '{"state":"MITIGATING"}', '{"state":"ESCALATED"}', 'Incident reopened after rollback drill failure', 'corr-pm-004', 'audit-pm-004', TIMESTAMP WITH TIME ZONE '2026-04-17 07:55:00+00:00'),
    ('log-pm-005', 'proj-42', 'HUMAN', 'u-007', NULL, 'COUNTERSIGN', 'DEPENDENCY', 'DEP-P42-UP-2', '{"state":"NEGOTIATING"}', '{"state":"APPROVED"}', 'Target PM confirmed readiness', 'corr-pm-005', 'audit-pm-005', TIMESTAMP WITH TIME ZONE '2026-04-16 10:00:00+00:00'),
    ('log-pm-006', 'proj-88', 'AI', NULL, 'skill-exec-pm-5', 'DISMISS_AI_SUGGESTION', 'AI_SUGGESTION', 'sug-p88-m-dismissed', '{"state":"PENDING"}', '{"state":"DISMISSED"}', 'Stale context after manual mitigation', 'corr-pm-006', 'audit-pm-006', TIMESTAMP WITH TIME ZONE '2026-04-17 09:15:00+00:00'),
    ('log-pm-007', 'proj-42', 'AI', NULL, 'skill-exec-pm-4', 'ACCEPT_AI_SUGGESTION', 'AI_SUGGESTION', 'sug-p42-r-accepted', '{"state":"PENDING"}', '{"state":"ACCEPTED"}', 'Historical acceptance snapshot', 'corr-pm-007', 'audit-pm-007', TIMESTAMP WITH TIME ZONE '2026-04-16 08:25:00+00:00'),
    ('log-pm-008', 'proj-55', 'HUMAN', 'u-007', NULL, 'CREATE', 'DEPENDENCY', 'dep-bootstrap-1', NULL, '{"state":"PROPOSED"}', 'Dependency lane seeded for PM slice', 'corr-pm-008', 'audit-pm-008', TIMESTAMP WITH TIME ZONE '2026-04-17 07:10:00+00:00');
