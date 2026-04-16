package com.sdlctower.domain.requirement.dto;

import java.util.List;

public record ImportInspectionDto(
        String sourceFileName,
        String sourceKind,
        int totalFiles,
        int parsedFiles,
        int manualReviewFiles,
        int skippedFiles,
        List<ImportInspectionFileDto> files
) {}
