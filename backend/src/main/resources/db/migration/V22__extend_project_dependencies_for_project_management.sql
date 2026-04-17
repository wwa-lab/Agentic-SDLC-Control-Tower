ALTER TABLE project_dependencies
    ADD COLUMN pm_state VARCHAR(32);

ALTER TABLE project_dependencies
    ADD COLUMN contract_commitment CLOB;

ALTER TABLE project_dependencies
    ADD COLUMN rejection_reason CLOB;

ALTER TABLE project_dependencies
    ADD COLUMN counter_signed_by VARCHAR(64);

ALTER TABLE project_dependencies
    ADD COLUMN counter_signed_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE project_dependencies
    ADD COLUMN plan_revision_at_update BIGINT NOT NULL DEFAULT 0;

UPDATE project_dependencies
SET pm_state = CASE
        WHEN blocker_reason IS NOT NULL THEN 'NEGOTIATING'
        ELSE 'PROPOSED'
    END,
    plan_revision_at_update = 0;
