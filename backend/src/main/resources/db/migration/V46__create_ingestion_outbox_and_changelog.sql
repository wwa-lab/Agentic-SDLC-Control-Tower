CREATE TABLE ingestion_outbox (
    id VARCHAR(64) PRIMARY KEY,
    delivery_id VARCHAR(64) UNIQUE,
    event_type VARCHAR(64),
    raw_body CLOB,
    received_at TIMESTAMP,
    status VARCHAR(16) DEFAULT 'PENDING',
    error_payload VARCHAR(4096),
    processed_at TIMESTAMP
);

CREATE INDEX idx_ingestion_outbox_status_received ON ingestion_outbox(status, received_at);

CREATE TABLE code_build_change_log (
    id VARCHAR(64) PRIMARY KEY,
    entity_type VARCHAR(64),
    entity_id VARCHAR(64),
    entry_type VARCHAR(64),
    actor_id VARCHAR(64),
    detail VARCHAR(4096),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_code_build_change_log_entity ON code_build_change_log(entity_type, entity_id, created_at DESC);
