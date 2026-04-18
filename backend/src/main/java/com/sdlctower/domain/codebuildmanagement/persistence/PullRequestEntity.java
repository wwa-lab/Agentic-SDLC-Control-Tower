package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "pull_request")
public class PullRequestEntity {

    @Id
    private String id;

    @Column(name = "repo_id", nullable = false)
    private String repoId;

    @Column(name = "pr_number", nullable = false)
    private int prNumber;

    @Column(nullable = false)
    private String title;

    private String author;

    @Column(name = "source_branch")
    private String sourceBranch;

    @Column(name = "target_branch")
    private String targetBranch;

    private String state;

    @Column(name = "head_sha")
    private String headSha;

    @Column(name = "is_bot_authored")
    private boolean isBotAuthored;

    @Column(name = "external_url")
    private String externalUrl;

    @Lob
    @Column(name = "body_text")
    private String bodyText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PullRequestEntity() {}

    public String getId() { return id; }
    public String getRepoId() { return repoId; }
    public int getPrNumber() { return prNumber; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getSourceBranch() { return sourceBranch; }
    public String getTargetBranch() { return targetBranch; }
    public String getState() { return state; }
    public String getHeadSha() { return headSha; }
    public boolean isBotAuthored() { return isBotAuthored; }
    public String getExternalUrl() { return externalUrl; }
    public String getBodyText() { return bodyText; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
