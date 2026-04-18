CREATE TABLE report_fact_lead_time (
  id                BIGINT       NOT NULL PRIMARY KEY,
  org_id            VARCHAR(64)  NOT NULL,
  workspace_id      VARCHAR(64)  NOT NULL,
  project_id        VARCHAR(64)  NOT NULL,
  team_id           VARCHAR(64),
  requirement_type  VARCHAR(64),
  bucket_date       DATE         NOT NULL,
  lead_time_minutes INTEGER      NOT NULL,
  snapshot_at       TIMESTAMP    NOT NULL
);
CREATE INDEX idx_rf_lt_scope ON report_fact_lead_time(workspace_id, project_id, bucket_date);

CREATE TABLE report_fact_cycle_time (
  id                 BIGINT       NOT NULL PRIMARY KEY,
  org_id             VARCHAR(64)  NOT NULL,
  workspace_id       VARCHAR(64)  NOT NULL,
  project_id         VARCHAR(64)  NOT NULL,
  team_id            VARCHAR(64),
  stage              VARCHAR(32)  NOT NULL,
  bucket_date        DATE         NOT NULL,
  cycle_time_minutes INTEGER      NOT NULL,
  snapshot_at        TIMESTAMP    NOT NULL
);
CREATE INDEX idx_rf_ct_scope ON report_fact_cycle_time(workspace_id, project_id, bucket_date);

CREATE TABLE report_fact_throughput (
  id              BIGINT       NOT NULL PRIMARY KEY,
  org_id          VARCHAR(64)  NOT NULL,
  workspace_id    VARCHAR(64)  NOT NULL,
  project_id      VARCHAR(64)  NOT NULL,
  team_id         VARCHAR(64),
  week_start      DATE         NOT NULL,
  items_completed INTEGER      NOT NULL,
  snapshot_at     TIMESTAMP    NOT NULL
);
CREATE INDEX idx_rf_tp_scope_week ON report_fact_throughput(workspace_id, project_id, week_start);

CREATE TABLE report_fact_wip (
  id           BIGINT       NOT NULL PRIMARY KEY,
  org_id       VARCHAR(64)  NOT NULL,
  workspace_id VARCHAR(64)  NOT NULL,
  project_id   VARCHAR(64)  NOT NULL,
  team_id      VARCHAR(64),
  owner_id     VARCHAR(64),
  stage        VARCHAR(32)  NOT NULL,
  age_bucket   VARCHAR(16)  NOT NULL,
  item_count   INTEGER      NOT NULL,
  snapshot_at  TIMESTAMP    NOT NULL
);
CREATE INDEX idx_rf_wip_scope ON report_fact_wip(workspace_id, project_id, stage);

CREATE TABLE report_fact_flow_efficiency (
  id             BIGINT       NOT NULL PRIMARY KEY,
  org_id         VARCHAR(64)  NOT NULL,
  workspace_id   VARCHAR(64)  NOT NULL,
  project_id     VARCHAR(64)  NOT NULL,
  team_id        VARCHAR(64),
  stage          VARCHAR(32)  NOT NULL,
  bucket_date    DATE         NOT NULL,
  active_minutes INTEGER      NOT NULL,
  total_minutes  INTEGER      NOT NULL,
  snapshot_at    TIMESTAMP    NOT NULL
);
CREATE INDEX idx_rf_fe_scope ON report_fact_flow_efficiency(workspace_id, project_id, stage, bucket_date);
