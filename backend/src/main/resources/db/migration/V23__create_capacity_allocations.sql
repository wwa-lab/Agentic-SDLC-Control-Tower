CREATE TABLE capacity_allocations (
    id VARCHAR(64) PRIMARY KEY,
    project_id VARCHAR(64) NOT NULL,
    member_id VARCHAR(64) NOT NULL,
    milestone_id VARCHAR(64) NOT NULL,
    allocation_percent INT NOT NULL,
    justification VARCHAR(512),
    window_start DATE NOT NULL,
    window_end DATE NOT NULL,
    plan_revision BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_capacity_project_member_milestone
    ON capacity_allocations (project_id, member_id, milestone_id);

CREATE INDEX idx_capacity_project_window
    ON capacity_allocations (project_id, window_start);

CREATE INDEX idx_capacity_member_window
    ON capacity_allocations (member_id, window_start);
