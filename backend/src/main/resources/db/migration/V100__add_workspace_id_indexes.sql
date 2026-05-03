-- MTF-1: Add composite (workspace_id, ...) indexes on existing workspace-scoped tables
-- that had only a plain workspace_id column and no workspace-leading composite index.

CREATE INDEX idx_requirement_ws_status     ON requirement(workspace_id, status);
CREATE INDEX idx_requirement_ws_priority   ON requirement(workspace_id, priority, created_at);

CREATE INDEX idx_test_case_ws_project      ON test_case(workspace_id, project_id);
CREATE INDEX idx_test_run_ws_plan          ON test_run(workspace_id, plan_id, started_at);

CREATE INDEX idx_dp_application_ws_project ON dp_application(workspace_id, project_id);
CREATE INDEX idx_jenkins_instance_ws       ON jenkins_instance(workspace_id, name);

-- Unique key on PLATFORM_WORKSPACE.workspace_key to support fast by-key lookups.
CREATE UNIQUE INDEX ux_platform_workspace_key ON PLATFORM_WORKSPACE(workspace_key);
