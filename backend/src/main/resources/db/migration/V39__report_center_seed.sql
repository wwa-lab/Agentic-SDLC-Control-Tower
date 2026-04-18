INSERT INTO report_fact_lead_time (id, org_id, workspace_id, project_id, team_id, requirement_type, bucket_date, lead_time_minutes, snapshot_at) VALUES
  (1, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', 'business', DATE '2026-04-01', 4200, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (2, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', 'business', DATE '2026-04-04', 6300, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (3, 'org-default-001', 'ws-default-001', 'proj-11', 'team-beta', 'compliance', DATE '2026-04-05', 5100, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (4, 'org-default-001', 'ws-default-001', 'proj-55', 'team-gamma', 'platform', DATE '2026-04-10', 8100, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (5, 'org-default-001', 'ws-legacy-001', 'proj-07', 'team-beta', 'business', DATE '2026-04-12', 9300, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00');

INSERT INTO report_fact_cycle_time (id, org_id, workspace_id, project_id, team_id, stage, bucket_date, cycle_time_minutes, snapshot_at) VALUES
  (101, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', 'discovery', DATE '2026-04-05', 210, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (102, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', 'build', DATE '2026-04-05', 320, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (103, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', 'review', DATE '2026-04-05', 280, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (104, 'org-default-001', 'ws-default-001', 'proj-11', 'team-beta', 'discovery', DATE '2026-04-06', 180, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (105, 'org-default-001', 'ws-default-001', 'proj-11', 'team-beta', 'build', DATE '2026-04-06', 360, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (106, 'org-default-001', 'ws-default-001', 'proj-11', 'team-beta', 'review', DATE '2026-04-06', 340, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00');

INSERT INTO report_fact_throughput (id, org_id, workspace_id, project_id, team_id, week_start, items_completed, snapshot_at) VALUES
  (201, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', DATE '2026-03-23', 12, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (202, 'org-default-001', 'ws-default-001', 'proj-11', 'team-beta', DATE '2026-03-23', 7, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (203, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', DATE '2026-03-30', 15, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (204, 'org-default-001', 'ws-default-001', 'proj-11', 'team-beta', DATE '2026-03-30', 9, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (205, 'org-default-001', 'ws-default-001', 'proj-55', 'team-gamma', DATE '2026-04-06', 18, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (206, 'org-default-001', 'ws-default-001', 'proj-88', 'team-beta', DATE '2026-04-06', 11, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00');

INSERT INTO report_fact_wip (id, org_id, workspace_id, project_id, team_id, owner_id, stage, age_bucket, item_count, snapshot_at) VALUES
  (301, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', 'u-011', 'discovery', '0-3d', 4, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (302, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', 'u-011', 'discovery', '3-7d', 2, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (303, 'org-default-001', 'ws-default-001', 'proj-11', 'team-beta', 'u-020', 'build', '0-3d', 6, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (304, 'org-default-001', 'ws-default-001', 'proj-11', 'team-beta', 'u-020', 'review', '7-14d', 5, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (305, 'org-default-001', 'ws-default-001', 'proj-55', 'team-gamma', 'u-044', 'deploy', '14d+', 3, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00');

INSERT INTO report_fact_flow_efficiency (id, org_id, workspace_id, project_id, team_id, stage, bucket_date, active_minutes, total_minutes, snapshot_at) VALUES
  (401, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', 'discovery', DATE '2026-04-05', 640, 1228, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (402, 'org-default-001', 'ws-default-001', 'proj-42', 'team-alpha', 'build', DATE '2026-04-05', 910, 1915, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (403, 'org-default-001', 'ws-default-001', 'proj-11', 'team-beta', 'review', DATE '2026-04-06', 390, 1225, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00'),
  (404, 'org-default-001', 'ws-default-001', 'proj-55', 'team-gamma', 'deploy', DATE '2026-04-07', 520, 820, TIMESTAMP WITH TIME ZONE '2026-04-18 10:00:00+00:00');
