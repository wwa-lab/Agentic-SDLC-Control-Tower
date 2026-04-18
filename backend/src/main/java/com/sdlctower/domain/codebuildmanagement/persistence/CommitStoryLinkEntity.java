package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "commit_story_link")
public class CommitStoryLinkEntity {

    @Id
    private String id;

    @Column(name = "commit_id", nullable = false)
    private String commitId;

    @Column(name = "story_id")
    private String storyId;

    @Column(name = "link_status")
    private String linkStatus;

    @Column(name = "candidate_story_id")
    private String candidateStoryId;

    protected CommitStoryLinkEntity() {}

    public String getId() { return id; }
    public String getCommitId() { return commitId; }
    public String getStoryId() { return storyId; }
    public String getLinkStatus() { return linkStatus; }
    public String getCandidateStoryId() { return candidateStoryId; }
}
