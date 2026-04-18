CREATE TABLE commit_story_link (
    id VARCHAR(64) PRIMARY KEY,
    commit_id VARCHAR(64) NOT NULL,
    story_id VARCHAR(64),
    link_status VARCHAR(32),
    candidate_story_id VARCHAR(64),
    CONSTRAINT fk_commit_story_link_commit FOREIGN KEY (commit_id) REFERENCES git_commit (id),
    CONSTRAINT uq_commit_story_link UNIQUE (commit_id, story_id)
);

CREATE INDEX idx_commit_story_link_story ON commit_story_link(story_id);
CREATE INDEX idx_commit_story_link_status ON commit_story_link(link_status);

CREATE TABLE pr_story_link (
    id VARCHAR(64) PRIMARY KEY,
    pr_id VARCHAR(64) NOT NULL,
    story_id VARCHAR(64),
    link_status VARCHAR(32),
    candidate_story_id VARCHAR(64),
    CONSTRAINT fk_pr_story_link_pr FOREIGN KEY (pr_id) REFERENCES pull_request (id),
    CONSTRAINT uq_pr_story_link UNIQUE (pr_id, story_id)
);

CREATE INDEX idx_pr_story_link_story ON pr_story_link(story_id);
