-- MTF-1: Backfill workspace_id on 7 control-plane tables from requirement.workspace_id.
-- All tables have direct or one-hop requirement_id FK.

UPDATE requirement_agent_run r
   SET workspace_id = (SELECT q.workspace_id FROM requirement q WHERE q.id = r.requirement_id)
 WHERE r.workspace_id IS NULL;

UPDATE requirement_source_reference s
   SET workspace_id = (SELECT q.workspace_id FROM requirement q WHERE q.id = s.requirement_id)
 WHERE s.workspace_id IS NULL;

UPDATE requirement_sdd_document_index d
   SET workspace_id = (SELECT q.workspace_id FROM requirement q WHERE q.id = d.requirement_id)
 WHERE d.workspace_id IS NULL;

UPDATE requirement_document_review v
   SET workspace_id = (SELECT q.workspace_id FROM requirement q WHERE q.id = v.requirement_id)
 WHERE v.workspace_id IS NULL;

UPDATE requirement_agent_stage_event e
   SET workspace_id = (SELECT r.workspace_id FROM requirement_agent_run r WHERE r.execution_id = e.execution_id)
 WHERE e.workspace_id IS NULL;

UPDATE requirement_document_quality_gate_run g
   SET workspace_id = (SELECT q.workspace_id FROM requirement q WHERE q.id = g.requirement_id)
 WHERE g.workspace_id IS NULL;

UPDATE requirement_artifact_link a
   SET workspace_id = (SELECT r.workspace_id FROM requirement_agent_run r WHERE r.execution_id = a.execution_id)
 WHERE a.workspace_id IS NULL;

-- Verification: each count must be 0 before V99 runs.
-- SELECT 'requirement_agent_run'              , COUNT(*) FROM requirement_agent_run              WHERE workspace_id IS NULL
-- UNION ALL SELECT 'requirement_source_reference'         , COUNT(*) FROM requirement_source_reference         WHERE workspace_id IS NULL
-- UNION ALL SELECT 'requirement_sdd_document_index'       , COUNT(*) FROM requirement_sdd_document_index       WHERE workspace_id IS NULL
-- UNION ALL SELECT 'requirement_document_review'          , COUNT(*) FROM requirement_document_review          WHERE workspace_id IS NULL
-- UNION ALL SELECT 'requirement_agent_stage_event'        , COUNT(*) FROM requirement_agent_stage_event        WHERE workspace_id IS NULL
-- UNION ALL SELECT 'requirement_document_quality_gate_run', COUNT(*) FROM requirement_document_quality_gate_run WHERE workspace_id IS NULL
-- UNION ALL SELECT 'requirement_artifact_link'            , COUNT(*) FROM requirement_artifact_link            WHERE workspace_id IS NULL;
