package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pr_story_link")
public class PrStoryLinkEntity {

    @Id
    private String id;

    @Column(name = "pr_id", nullable = false)
    private String prId;

    @Column(name = "story_id")
    private String storyId;

    @Column(name = "link_status")
    private String linkStatus;

    @Column(name = "candidate_story_id")
    private String candidateStoryId;

    protected PrStoryLinkEntity() {}

    public String getId() { return id; }
    public String getPrId() { return prId; }
    public String getStoryId() { return storyId; }
    public String getLinkStatus() { return linkStatus; }
    public String getCandidateStoryId() { return candidateStoryId; }
}
