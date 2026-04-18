package com.sdlctower.domain.aicenter.dto;

import java.util.List;

public record StageCoverageDto(
        List<StageCoverageEntryDto> entries
) {}
