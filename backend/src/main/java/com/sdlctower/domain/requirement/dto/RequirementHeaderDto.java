package com.sdlctower.domain.requirement.dto;

public record RequirementHeaderDto(
        String id,
        String title,
        String priority,
        String status,
        String category,
        String source,
        String assignee,
        int completenessScore,
        int storyCount,
        int specCount,
        String createdAt,
        String updatedAt
) {}
