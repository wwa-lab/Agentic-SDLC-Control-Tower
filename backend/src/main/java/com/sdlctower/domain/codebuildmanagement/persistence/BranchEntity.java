package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "branch")
public class BranchEntity {

    @Id
    private String id;

    @Column(name = "repo_id", nullable = false)
    private String repoId;

    @Column(nullable = false)
    private String name;

    @Column(name = "head_sha")
    private String headSha;

    @Column(name = "ahead_of_default")
    private int aheadOfDefault;

    @Column(name = "behind_default")
    private int behindDefault;

    @Column(name = "last_commit_at")
    private Instant lastCommitAt;

    protected BranchEntity() {}

    public String getId() { return id; }
    public String getRepoId() { return repoId; }
    public String getName() { return name; }
    public String getHeadSha() { return headSha; }
    public int getAheadOfDefault() { return aheadOfDefault; }
    public int getBehindDefault() { return behindDefault; }
    public Instant getLastCommitAt() { return lastCommitAt; }
}
