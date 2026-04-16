package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatusDistributionDto(
        int draft,
        int inReview,
        int approved,
        int inProgress,
        int delivered,
        int archived
) {

    @JsonProperty("DRAFT")
    public int draftUpper() {
        return draft;
    }

    @JsonProperty("IN_REVIEW")
    public int inReviewUpper() {
        return inReview;
    }

    @JsonProperty("APPROVED")
    public int approvedUpper() {
        return approved;
    }

    @JsonProperty("IN_PROGRESS")
    public int inProgressUpper() {
        return inProgress;
    }

    @JsonProperty("DELIVERED")
    public int deliveredUpper() {
        return delivered;
    }

    @JsonProperty("ARCHIVED")
    public int archivedUpper() {
        return archived;
    }
}
