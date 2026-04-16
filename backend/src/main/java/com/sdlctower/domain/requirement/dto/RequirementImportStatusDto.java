package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequirementImportStatusDto(
        String importId,
        String taskId,
        String status,
        String message,
        String knowledgeBaseName,
        String datasetId,
        int totalNumberOfFiles,
        int numberOfSuccesses,
        int numberOfFailures,
        long totalSize,
        List<String> unsupportedFileTypes,
        List<String> supportedFileTypes,
        List<RequirementImportFileStatusDto> files,
        RequirementDraftDto draft,
        String createdAt,
        String updatedAt
) {}
