CREATE TABLE requirement_document_review (
    id VARCHAR(64) PRIMARY KEY,
    document_id VARCHAR(64) NOT NULL,
    requirement_id VARCHAR(20) NOT NULL,
    decision VARCHAR(64) NOT NULL,
    comment CLOB,
    reviewer_id VARCHAR(128) NOT NULL,
    reviewer_type VARCHAR(64) NOT NULL,
    commit_sha VARCHAR(128) NOT NULL,
    blob_sha VARCHAR(128) NOT NULL,
    anchor_type VARCHAR(64),
    anchor_value VARCHAR(255),
    stale BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_req_doc_review_document FOREIGN KEY (document_id) REFERENCES requirement_sdd_document_index (id),
    CONSTRAINT fk_req_doc_review_requirement FOREIGN KEY (requirement_id) REFERENCES requirement (id)
);

INSERT INTO requirement_document_review (
    id, document_id, requirement_id, decision, comment, reviewer_id, reviewer_type, commit_sha, blob_sha, anchor_type, anchor_value, stale, created_at
) VALUES
    ('REV-REQ-0001-SPEC-1', 'DOC-REQ-0001-SPEC', 'REQ-0001', 'APPROVED', 'Business scope is approved for the reviewed version.', 'u-business-001', 'BUSINESS', 'c0ffee1002', 'blob-spec-0001-v1', 'DOCUMENT', NULL, TRUE, TIMESTAMP WITH TIME ZONE '2026-04-15 14:00:00+00:00');
