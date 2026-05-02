-- Align legacy requirement seed data with the platform workspace id used by
-- workspace-scoped API routes and Hibernate workspace filtering.
UPDATE requirement
   SET workspace_id = 'ws-default-001'
 WHERE workspace_id = 'GLOBAL-SDLC-TOWER';

UPDATE requirement_agent_run
   SET workspace_id = 'ws-default-001'
 WHERE workspace_id = 'GLOBAL-SDLC-TOWER';

UPDATE requirement_source_reference
   SET workspace_id = 'ws-default-001'
 WHERE workspace_id = 'GLOBAL-SDLC-TOWER';

UPDATE requirement_sdd_document_index
   SET workspace_id = 'ws-default-001'
 WHERE workspace_id = 'GLOBAL-SDLC-TOWER';

UPDATE requirement_document_review
   SET workspace_id = 'ws-default-001'
 WHERE workspace_id = 'GLOBAL-SDLC-TOWER';

UPDATE requirement_agent_stage_event
   SET workspace_id = 'ws-default-001'
 WHERE workspace_id = 'GLOBAL-SDLC-TOWER';

UPDATE requirement_document_quality_gate_run
   SET workspace_id = 'ws-default-001'
 WHERE workspace_id = 'GLOBAL-SDLC-TOWER';

UPDATE requirement_artifact_link
   SET workspace_id = 'ws-default-001'
 WHERE workspace_id = 'GLOBAL-SDLC-TOWER';
