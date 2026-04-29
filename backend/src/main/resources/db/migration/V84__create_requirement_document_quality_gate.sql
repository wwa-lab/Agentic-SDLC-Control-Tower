CREATE TABLE requirement_document_quality_gate_run (
    execution_id VARCHAR(80) PRIMARY KEY,
    document_id VARCHAR(64) NOT NULL,
    requirement_id VARCHAR(32) NOT NULL,
    profile_id VARCHAR(64) NOT NULL,
    sdd_type VARCHAR(80) NOT NULL,
    score INTEGER NOT NULL,
    band VARCHAR(24) NOT NULL,
    passed BOOLEAN NOT NULL,
    threshold INTEGER NOT NULL,
    rubric_version VARCHAR(160) NOT NULL,
    commit_sha VARCHAR(80) NOT NULL,
    blob_sha VARCHAR(80) NOT NULL,
    dimensions CLOB NOT NULL,
    findings CLOB NOT NULL,
    summary CLOB NOT NULL,
    triggered_by VARCHAR(120) NOT NULL,
    trigger_mode VARCHAR(24) NOT NULL,
    scored_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_req_doc_quality_document FOREIGN KEY (document_id) REFERENCES requirement_sdd_document_index (id),
    CONSTRAINT fk_req_doc_quality_requirement FOREIGN KEY (requirement_id) REFERENCES requirement (id)
);

CREATE INDEX idx_req_doc_quality_document ON requirement_document_quality_gate_run (document_id, scored_at);
CREATE INDEX idx_req_doc_quality_requirement ON requirement_document_quality_gate_run (requirement_id, scored_at);
