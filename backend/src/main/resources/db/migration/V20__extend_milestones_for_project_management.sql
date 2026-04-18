ALTER TABLE milestones
    ADD COLUMN description CLOB;

ALTER TABLE milestones
    ADD COLUMN pm_state VARCHAR(32);

ALTER TABLE milestones
    ADD COLUMN baseline_target_date DATE;

ALTER TABLE milestones
    ADD COLUMN slippage_risk_score VARCHAR(16);

ALTER TABLE milestones
    ADD COLUMN slippage_risk_factors CLOB;

ALTER TABLE milestones
    ADD COLUMN plan_revision_at_update BIGINT NOT NULL DEFAULT 0;

ALTER TABLE milestones
    ADD COLUMN ai_suggestion_id VARCHAR(64);

ALTER TABLE milestones
    ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE milestones
    ADD COLUMN archived_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE project_plan_revisions (
    project_id VARCHAR(64) PRIMARY KEY,
    current_revision BIGINT NOT NULL DEFAULT 0
);

UPDATE milestones
SET pm_state = CASE
        WHEN status = 'AT_RISK' THEN 'AT_RISK'
        WHEN status = 'COMPLETED' THEN 'COMPLETED'
        WHEN status = 'IN_PROGRESS' THEN 'IN_PROGRESS'
        ELSE 'NOT_STARTED'
    END,
    baseline_target_date = target_date,
    completed_at = CASE WHEN status = 'COMPLETED' THEN updated_at ELSE NULL END,
    plan_revision_at_update = 0;
