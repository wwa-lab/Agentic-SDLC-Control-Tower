CREATE TABLE log_excerpt (
    id VARCHAR(64) PRIMARY KEY,
    step_id VARCHAR(64) NOT NULL,
    text CLOB,
    byte_count INT,
    external_url VARCHAR(512),
    CONSTRAINT fk_log_excerpt_step FOREIGN KEY (step_id) REFERENCES pipeline_step (id),
    CONSTRAINT chk_log_excerpt_byte_limit CHECK (byte_count <= 1048576)
);

CREATE INDEX idx_log_excerpt_step ON log_excerpt(step_id);
