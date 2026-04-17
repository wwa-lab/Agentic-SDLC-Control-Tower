CREATE TABLE design_artifact_author (
    artifact_id VARCHAR(64) NOT NULL,
    member_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (artifact_id, member_id),
    CONSTRAINT fk_design_artifact_author_artifact FOREIGN KEY (artifact_id) REFERENCES design_artifact (id)
);

CREATE INDEX idx_design_artifact_author_member
    ON design_artifact_author (member_id);
