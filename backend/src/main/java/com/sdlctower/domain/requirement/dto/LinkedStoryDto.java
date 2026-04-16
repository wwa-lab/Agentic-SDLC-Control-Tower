package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LinkedStoryDto(
        String id,
        String title,
        String status,
        String specId,
        String specStatus
) {}
