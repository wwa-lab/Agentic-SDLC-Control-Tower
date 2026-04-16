CREATE TABLE risk_signals (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64) NOT NULL,
    category VARCHAR(32) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    source_kind VARCHAR(32) NOT NULL,
    source_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    detail CLOB NOT NULL,
    action_label VARCHAR(128) NOT NULL,
    action_url VARCHAR(255) NOT NULL,
    skill_name VARCHAR(128),
    execution_id VARCHAR(128),
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_risk_signals_workspace_detected
    ON risk_signals (workspace_id, detected_at);

CREATE TABLE metric_snapshots (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64) NOT NULL,
    metric_group VARCHAR(64) NOT NULL,
    metric_key VARCHAR(128) NOT NULL,
    metric_label VARCHAR(128) NOT NULL,
    metric_unit VARCHAR(32) NOT NULL,
    current_value DOUBLE PRECISION NOT NULL,
    previous_value DOUBLE PRECISION NOT NULL,
    trend_direction VARCHAR(16) NOT NULL,
    history_url VARCHAR(255) NOT NULL,
    tooltip CLOB NOT NULL,
    snapshot_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_metric_snapshots_workspace_group
    ON metric_snapshots (workspace_id, metric_group, snapshot_at);
