CREATE TABLE dp_change_log (
    id VARCHAR(64) PRIMARY KEY,
    entity_type VARCHAR(32) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    entry_type VARCHAR(64) NOT NULL,
    actor VARCHAR(128),
    detail VARCHAR(2048),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_dplog_entity ON dp_change_log(entity_type, entity_id, created_at DESC);

CREATE TABLE dp_ingestion_outbox (
    id VARCHAR(64) PRIMARY KEY,
    delivery_id VARCHAR(128) NOT NULL,
    jenkins_instance_id VARCHAR(64),
    event_type VARCHAR(32),
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    raw_body CLOB,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    error_message VARCHAR(1024),
    CONSTRAINT uq_dp_outbox_delivery UNIQUE (delivery_id)
);
CREATE INDEX idx_dpoutbox_status ON dp_ingestion_outbox(status, received_at);

CREATE TABLE dp_backfill_checkpoint (
    id VARCHAR(64) PRIMARY KEY,
    jenkins_instance_id VARCHAR(64) NOT NULL,
    last_build_timestamp TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
