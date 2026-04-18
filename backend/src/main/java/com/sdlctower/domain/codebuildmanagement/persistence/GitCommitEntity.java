package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "git_commit")
public class GitCommitEntity {

    @Id
    private String id;

    @Column(name = "repo_id", nullable = false)
    private String repoId;

    @Column(nullable = false)
    private String sha;

    @Column(name = "short_sha")
    private String shortSha;

    private String author;

    @Column(length = 4096)
    private String message;

    @Column(name = "committed_at")
    private Instant committedAt;

    @Column(name = "branch_name")
    private String branchName;

    protected GitCommitEntity() {}

    public String getId() { return id; }
    public String getRepoId() { return repoId; }
    public String getSha() { return sha; }
    public String getShortSha() { return shortSha; }
    public String getAuthor() { return author; }
    public String getMessage() { return message; }
    public Instant getCommittedAt() { return committedAt; }
    public String getBranchName() { return branchName; }
}
