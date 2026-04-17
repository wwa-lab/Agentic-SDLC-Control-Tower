ALTER TABLE risk_signals
    ADD COLUMN project_id VARCHAR(64);

CREATE INDEX idx_risk_signals_project_detected
    ON risk_signals (project_id, detected_at);
