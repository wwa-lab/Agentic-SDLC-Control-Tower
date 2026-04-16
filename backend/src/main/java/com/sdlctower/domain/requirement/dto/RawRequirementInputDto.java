package com.sdlctower.domain.requirement.dto;

import java.util.List;

public record RawRequirementInputDto(
        String sourceType,
        String text,
        String fileName,
        Long fileSize,
        List<String> fileNames,
        Integer fileCount,
        String kbName
) {}
