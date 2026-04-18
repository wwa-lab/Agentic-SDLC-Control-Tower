ALTER TABLE risk_signals
    ADD COLUMN pm_state VARCHAR(32);

ALTER TABLE risk_signals
    ADD COLUMN owner_member_id VARCHAR(64);

ALTER TABLE risk_signals
    ADD COLUMN mitigation_note CLOB;

ALTER TABLE risk_signals
    ADD COLUMN resolution_note CLOB;

ALTER TABLE risk_signals
    ADD COLUMN escalated_incident_id VARCHAR(64);

ALTER TABLE risk_signals
    ADD COLUMN plan_revision_at_update BIGINT NOT NULL DEFAULT 0;

UPDATE risk_signals
SET pm_state = CASE
        WHEN resolved_at IS NOT NULL THEN 'RESOLVED'
        ELSE 'IDENTIFIED'
    END,
    plan_revision_at_update = 0;
