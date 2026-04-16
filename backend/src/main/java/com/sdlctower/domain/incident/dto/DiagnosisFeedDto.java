package com.sdlctower.domain.incident.dto;

import java.util.List;

public record DiagnosisFeedDto(
        List<DiagnosisEntryDto> entries,
        RootCauseHypothesisDto rootCause,
        List<String> affectedComponents
) {}
