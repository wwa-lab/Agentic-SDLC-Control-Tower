ALTER TABLE workspace_context ADD workspace_id VARCHAR(64);
ALTER TABLE workspace_context ADD application_id VARCHAR(64);
ALTER TABLE workspace_context ADD snow_group_id VARCHAR(64);
ALTER TABLE workspace_context ADD project_id VARCHAR(64);
ALTER TABLE workspace_context ADD demo_mode BOOLEAN DEFAULT FALSE NOT NULL;

UPDATE workspace_context
SET workspace_id = 'ws-default-001',
    application_id = 'app-payment-gateway-pro',
    snow_group_id = 'snow-fin-tech-ops',
    project_id = 'proj-42',
    demo_mode = FALSE;
