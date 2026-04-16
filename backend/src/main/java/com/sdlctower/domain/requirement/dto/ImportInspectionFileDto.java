package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImportInspectionFileDto(
        String fileName,
        String fileType,
        String processingStatus,
        String summary,
        Integer extractedCharacters,
        String preview
) {}
