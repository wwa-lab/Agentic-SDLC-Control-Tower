-- MTF-1: Add nullable workspace_id to 7 control-plane tables.
-- Backfilled in V98, enforced NOT NULL in V99.

ALTER TABLE requirement_agent_run         ADD workspace_id VARCHAR(64);
ALTER TABLE requirement_source_reference  ADD workspace_id VARCHAR(64);
ALTER TABLE requirement_sdd_document_index ADD workspace_id VARCHAR(64);
ALTER TABLE requirement_document_review   ADD workspace_id VARCHAR(64);
ALTER TABLE requirement_agent_stage_event ADD workspace_id VARCHAR(64);
ALTER TABLE requirement_document_quality_gate_run ADD workspace_id VARCHAR(64);
ALTER TABLE requirement_artifact_link     ADD workspace_id VARCHAR(64);
