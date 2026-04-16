package com.sdlctower.domain.requirement.dto;

import java.util.List;

public record AiAnalysisDto(
        int completenessScore,
        List<String> missingElements,
        List<SimilarRequirementDto> similarRequirements,
        String impactAssessment,
        List<String> suggestions
) {}
