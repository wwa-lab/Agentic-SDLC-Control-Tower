-- MTF-1: Enforce NOT NULL and add composite indexes on the 7 control-plane tables.
-- Requires V98 backfill to have zero null rows.

ALTER TABLE requirement_agent_run              ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE requirement_source_reference       ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE requirement_sdd_document_index     ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE requirement_document_review        ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE requirement_agent_stage_event      ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE requirement_document_quality_gate_run ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE requirement_artifact_link          ALTER COLUMN workspace_id SET NOT NULL;

CREATE INDEX idx_req_agent_run_ws        ON requirement_agent_run(workspace_id, created_at);
CREATE INDEX idx_req_src_ref_ws          ON requirement_source_reference(workspace_id, requirement_id);
CREATE INDEX idx_req_sdd_doc_ws          ON requirement_sdd_document_index(workspace_id, requirement_id);
CREATE INDEX idx_req_doc_review_ws       ON requirement_document_review(workspace_id, document_id);
CREATE INDEX idx_req_agent_stage_evt_ws  ON requirement_agent_stage_event(workspace_id, execution_id);
CREATE INDEX idx_req_doc_qgr_ws          ON requirement_document_quality_gate_run(workspace_id, document_id);
CREATE INDEX idx_req_artifact_link_ws    ON requirement_artifact_link(workspace_id, execution_id);
