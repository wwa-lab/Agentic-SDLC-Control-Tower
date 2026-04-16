INSERT INTO risk_signals (
    id, workspace_id, category, severity, source_kind, source_id, title, detail,
    action_label, action_url, skill_name, execution_id, detected_at, resolved_at
) VALUES
    (
        'RISK-1001', 'ws-default-001', 'INCIDENT', 'CRITICAL', 'INCIDENT', 'INC-0422',
        'P1: payment-service outage', 'Payment throughput 0 rps for 12m',
        'Open Incident', '/incidents/INC-0422', NULL, NULL,
        TIMESTAMP WITH TIME ZONE '2026-04-17 10:00:00+00:00', NULL
    ),
    (
        'RISK-1002', 'ws-default-001', 'APPROVAL', 'HIGH', 'SPEC', 'SPEC-0088',
        '4 spec approvals pending > 3d', 'Oldest approval is waiting on SPEC-0088',
        'Review approvals', '/platform?view=approvals&workspaceId=ws-default-001', NULL, NULL,
        TIMESTAMP WITH TIME ZONE '2026-04-14 10:00:00+00:00', NULL
    ),
    (
        'RISK-1003', 'ws-default-001', 'CONFIG_DRIFT', 'MEDIUM', 'PROJECT', 'proj-42',
        'Approval mode overridden at project level for 3 projects', 'Projects: proj-42, proj-55, proj-88',
        'View in Platform Center', '/platform?view=config&workspaceId=ws-default-001&section=approval',
        'policy-drift-check', 'skill-run-3321',
        TIMESTAMP WITH TIME ZONE '2026-04-05 10:00:00+00:00', NULL
    ),
    (
        'RISK-2001', 'ws-legacy-001', 'DEPENDENCY', 'HIGH', 'PROJECT', 'proj-legacy-01',
        'Legacy dependency window is unsupported', 'Mainframe dependency has no approved backup owner',
        'Inspect dependency view', '/platform?view=config&workspaceId=ws-legacy-001&section=dependency',
        NULL, NULL,
        TIMESTAMP WITH TIME ZONE '2026-04-11 08:00:00+00:00', NULL
    );

INSERT INTO metric_snapshots (
    id, workspace_id, metric_group, metric_key, metric_label, metric_unit,
    current_value, previous_value, trend_direction, history_url, tooltip, snapshot_at
) VALUES
    (
        'MET-001', 'ws-default-001', 'deliveryEfficiency', 'delivery.cycleTime', 'Cycle Time', 'DAYS',
        4.2, 5.1, 'DOWN', '/reports/metric/delivery.cycleTime?workspaceId=ws-default-001',
        'Average days from Approved to Delivered',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-002', 'ws-default-001', 'deliveryEfficiency', 'delivery.throughput', 'Throughput', 'COUNT',
        18.0, 15.0, 'UP', '/reports/metric/delivery.throughput?workspaceId=ws-default-001',
        'Requirements delivered per week',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-003', 'ws-default-001', 'quality', 'quality.defectRate', 'Defect Rate', 'PERCENT',
        2.1, 2.8, 'DOWN', '/reports/metric/quality.defectRate?workspaceId=ws-default-001',
        'Escaped defects per release',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-004', 'ws-default-001', 'quality', 'quality.testPassRate', 'Test Pass Rate', 'PERCENT',
        96.0, 94.0, 'UP', '/reports/metric/quality.testPassRate?workspaceId=ws-default-001',
        'Automated suite pass rate',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-005', 'ws-default-001', 'stability', 'stability.mttr', 'MTTR', 'HOURS',
        1.8, 2.4, 'DOWN', '/reports/metric/stability.mttr?workspaceId=ws-default-001',
        'Mean time to recover critical incidents',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-006', 'ws-default-001', 'stability', 'stability.incidentFrequency', 'Incident Frequency', 'COUNT',
        3.0, 4.0, 'DOWN', '/reports/metric/stability.incidentFrequency?workspaceId=ws-default-001',
        'Open incidents in the last 30 days',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-007', 'ws-default-001', 'governanceMaturity', 'governance.approvalCoverage', 'Approval Coverage', 'PERCENT',
        91.0, 88.0, 'UP', '/reports/metric/governance.approvalCoverage?workspaceId=ws-default-001',
        'Specs with an explicit approval record',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-008', 'ws-default-001', 'governanceMaturity', 'governance.auditCompleteness', 'Audit Completeness', 'PERCENT',
        97.0, 95.0, 'UP', '/reports/metric/governance.auditCompleteness?workspaceId=ws-default-001',
        'Required audit fields present across workflows',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-009', 'ws-default-001', 'aiParticipation', 'ai.participationRate', 'AI Participation', 'PERCENT',
        74.0, 69.0, 'UP', '/reports/metric/ai.participationRate?workspaceId=ws-default-001',
        'Requirements with AI assistance in the chain',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-010', 'ws-default-001', 'aiParticipation', 'ai.acceptanceRate', 'AI Acceptance', 'PERCENT',
        63.0, 61.0, 'UP', '/reports/metric/ai.acceptanceRate?workspaceId=ws-default-001',
        'Accepted AI suggestions over the last 30 days',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    ),
    (
        'MET-011', 'ws-legacy-001', 'deliveryEfficiency', 'delivery.cycleTime', 'Cycle Time', 'DAYS',
        8.3, 7.9, 'UP', '/reports/metric/delivery.cycleTime?workspaceId=ws-legacy-001',
        'Average days from Approved to Delivered',
        TIMESTAMP WITH TIME ZONE '2026-04-17 01:00:00+00:00'
    );
