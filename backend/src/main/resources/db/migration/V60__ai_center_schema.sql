-- AI Center schema: skills, policies, executions, evidence

CREATE TABLE skill (
    id               VARCHAR(64)   NOT NULL PRIMARY KEY,
    workspace_id     VARCHAR(64)   NOT NULL,
    skill_key_code   VARCHAR(128)  NOT NULL,
    name             VARCHAR(256)  NOT NULL,
    category         VARCHAR(32)   NOT NULL,
    sub_category     VARCHAR(64),
    status           VARCHAR(16)   NOT NULL,
    default_autonomy VARCHAR(32)   NOT NULL,
    owner            VARCHAR(128)  NOT NULL,
    description      CLOB,
    input_contract   CLOB,
    output_contract  CLOB,
    version          INT           NOT NULL DEFAULT 1,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL
);
CREATE UNIQUE INDEX idx_skill_ws_key      ON skill (workspace_id, skill_key_code);
CREATE INDEX        idx_skill_ws_category ON skill (workspace_id, category);

CREATE TABLE skill_stage (
    id        VARCHAR(64) NOT NULL PRIMARY KEY,
    skill_id  VARCHAR(64) NOT NULL,
    stage_key VARCHAR(32) NOT NULL,
    CONSTRAINT fk_skill_stage_skill FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE
);
CREATE INDEX idx_skill_stage_skill ON skill_stage (skill_id);

CREATE TABLE policy (
    id                        VARCHAR(64)  NOT NULL PRIMARY KEY,
    workspace_id              VARCHAR(64)  NOT NULL,
    skill_id                  VARCHAR(64)  NOT NULL,
    autonomy_level            VARCHAR(32)  NOT NULL,
    approval_required_actions CLOB,
    authorized_approver_roles CLOB,
    risk_thresholds           CLOB,
    last_changed_at           TIMESTAMP    NOT NULL,
    last_changed_by           VARCHAR(128) NOT NULL,
    CONSTRAINT fk_policy_skill FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_policy_ws_skill ON policy (workspace_id, skill_id);

CREATE TABLE skill_execution (
    id                  VARCHAR(64)  NOT NULL PRIMARY KEY,
    workspace_id        VARCHAR(64)  NOT NULL,
    skill_id            VARCHAR(64)  NOT NULL,
    skill_key           VARCHAR(128) NOT NULL,
    status              VARCHAR(32)  NOT NULL,
    trigger_source_type VARCHAR(16)  NOT NULL,
    trigger_source_page VARCHAR(256),
    trigger_source_url  VARCHAR(512),
    triggered_by        VARCHAR(128) NOT NULL,
    triggered_by_type   VARCHAR(16)  NOT NULL,
    started_at          TIMESTAMP    NOT NULL,
    ended_at            TIMESTAMP,
    duration_ms         BIGINT,
    input_summary       CLOB,
    output_summary      CLOB,
    step_breakdown      CLOB,
    policy_trail        CLOB,
    autonomy_level      VARCHAR(32),
    time_saved_minutes  INT,
    audit_record_id     VARCHAR(64),
    outcome_summary     VARCHAR(512),
    CONSTRAINT fk_exec_skill FOREIGN KEY (skill_id) REFERENCES skill(id)
);
CREATE INDEX idx_exec_ws_started ON skill_execution (workspace_id, started_at DESC);
CREATE INDEX idx_exec_ws_skill   ON skill_execution (workspace_id, skill_id, started_at DESC);
CREATE INDEX idx_exec_ws_status  ON skill_execution (workspace_id, status);

CREATE TABLE evidence_link (
    id            VARCHAR(64)   NOT NULL PRIMARY KEY,
    execution_id  VARCHAR(64)   NOT NULL,
    title         VARCHAR(256)  NOT NULL,
    type          VARCHAR(32)   NOT NULL,
    source_system VARCHAR(64)   NOT NULL,
    url           VARCHAR(1024) NOT NULL,
    position      INT           NOT NULL,
    CONSTRAINT fk_ev_execution FOREIGN KEY (execution_id) REFERENCES skill_execution(id) ON DELETE CASCADE
);
CREATE INDEX idx_ev_execution ON evidence_link (execution_id, position);
