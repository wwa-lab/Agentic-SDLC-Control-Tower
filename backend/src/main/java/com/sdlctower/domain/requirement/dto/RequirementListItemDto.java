package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RequirementListItemDto(
        String id,
        String title,
        String priority,
        String status,
        String category,
        int storyCount,
        int specCount,
        int completenessScore,
        String assignee,
        String createdAt,
        String updatedAt
) {

    @JsonProperty("completeness")
    public int completeness() {
        return completenessScore;
    }
}
