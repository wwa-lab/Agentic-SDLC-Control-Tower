package com.sdlctower.domain.requirement.dto;

public record RequirementImportFileStatusDto(
        String displayName,
        String sourceFileName,
        String sourceKind,
        String fileExtension,
        long fileSize,
        boolean supported,
        String providerStatus,
        String errorMessage,
        String preview,
        String providerDocumentId
) {}
