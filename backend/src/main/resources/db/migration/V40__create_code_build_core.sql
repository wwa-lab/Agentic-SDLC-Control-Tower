CREATE TABLE repo (
    id VARCHAR(64) PRIMARY KEY,
    github_repo_id BIGINT,
    installation_id VARCHAR(64),
    full_name VARCHAR(255) NOT NULL,
    project_id VARCHAR(64) NOT NULL,
    workspace_id VARCHAR(64) NOT NULL,
    default_branch VARCHAR(255) DEFAULT 'main',
    primary_language VARCHAR(64),
    description VARCHAR(1024),
    visibility VARCHAR(16) NOT NULL DEFAULT 'PRIVATE',
    external_url VARCHAR(512),
    last_activity_at TIMESTAMP,
    last_synced_at TIMESTAMP,
    archived_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_repo_workspace_project ON repo(workspace_id, project_id);
CREATE INDEX idx_repo_full_name ON repo(full_name);

CREATE TABLE branch (
    id VARCHAR(64) PRIMARY KEY,
    repo_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    head_sha VARCHAR(40),
    ahead_of_default INT DEFAULT 0,
    behind_default INT DEFAULT 0,
    last_commit_at TIMESTAMP,
    CONSTRAINT fk_branch_repo FOREIGN KEY (repo_id) REFERENCES repo (id),
    CONSTRAINT uq_branch_repo_name UNIQUE (repo_id, name)
);

CREATE INDEX idx_branch_repo ON branch(repo_id);

CREATE TABLE git_commit (
    id VARCHAR(64) PRIMARY KEY,
    repo_id VARCHAR(64) NOT NULL,
    sha VARCHAR(40) NOT NULL,
    short_sha VARCHAR(12) NOT NULL,
    author VARCHAR(255),
    message VARCHAR(4096),
    committed_at TIMESTAMP NOT NULL,
    branch_name VARCHAR(255),
    CONSTRAINT fk_git_commit_repo FOREIGN KEY (repo_id) REFERENCES repo (id),
    CONSTRAINT uq_git_commit_repo_sha UNIQUE (repo_id, sha)
);

CREATE INDEX idx_git_commit_repo_date ON git_commit(repo_id, committed_at DESC);

CREATE TABLE pull_request (
    id VARCHAR(64) PRIMARY KEY,
    repo_id VARCHAR(64) NOT NULL,
    pr_number INT NOT NULL,
    title VARCHAR(512) NOT NULL,
    author VARCHAR(255),
    source_branch VARCHAR(255),
    target_branch VARCHAR(255),
    state VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    head_sha VARCHAR(40),
    is_bot_authored BOOLEAN DEFAULT FALSE,
    external_url VARCHAR(512),
    body_text CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pull_request_repo FOREIGN KEY (repo_id) REFERENCES repo (id),
    CONSTRAINT uq_pull_request_repo_number UNIQUE (repo_id, pr_number)
);

CREATE INDEX idx_pull_request_repo ON pull_request(repo_id, updated_at DESC);
CREATE INDEX idx_pull_request_head_sha ON pull_request(head_sha);
