CREATE TABLE design_spec_link (
    id VARCHAR(64) PRIMARY KEY,
    artifact_id VARCHAR(64) NOT NULL,
    spec_id VARCHAR(64) NOT NULL,
    covers_revision INT NOT NULL,
    declared_coverage VARCHAR(16) NOT NULL,
    linked_by_member_id VARCHAR(64) NOT NULL,
    linked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_design_spec_link UNIQUE (artifact_id, spec_id),
    CONSTRAINT fk_design_spec_link_artifact FOREIGN KEY (artifact_id) REFERENCES design_artifact (id)
);

CREATE INDEX idx_design_spec_link_spec
    ON design_spec_link (spec_id);
