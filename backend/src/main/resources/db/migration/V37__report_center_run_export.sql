CREATE TABLE report_run (
  id                 VARCHAR(36)  NOT NULL PRIMARY KEY,
  report_key         VARCHAR(64)  NOT NULL,
  user_id            VARCHAR(64)  NOT NULL,
  scope              VARCHAR(16)  NOT NULL,
  scope_ids_json     CLOB         NOT NULL,
  time_range_preset  VARCHAR(16)  NOT NULL,
  time_range_start   TIMESTAMP,
  time_range_end     TIMESTAMP,
  grouping           VARCHAR(64)  NOT NULL,
  extra_filters_json CLOB,
  run_at             TIMESTAMP    NOT NULL,
  duration_ms        INTEGER      NOT NULL DEFAULT 0,
  row_count          INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX idx_report_run_user_runat ON report_run(user_id, run_at DESC);
CREATE INDEX idx_report_run_key ON report_run(report_key);

CREATE TABLE report_export (
  id            VARCHAR(36)  NOT NULL PRIMARY KEY,
  report_run_id VARCHAR(36)  NOT NULL,
  user_id       VARCHAR(64)  NOT NULL,
  report_key    VARCHAR(64)  NOT NULL,
  format        VARCHAR(8)   NOT NULL,
  status        VARCHAR(16)  NOT NULL,
  download_url  VARCHAR(512),
  storage_path  VARCHAR(1024),
  error_message CLOB,
  bytes         BIGINT,
  created_at    TIMESTAMP    NOT NULL,
  ready_at      TIMESTAMP,
  expires_at    TIMESTAMP
);
CREATE INDEX idx_report_export_user_created ON report_export(user_id, created_at DESC);
CREATE INDEX idx_report_export_status ON report_export(status);
CREATE INDEX idx_report_export_expires ON report_export(expires_at);
