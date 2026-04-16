package com.sdlctower.domain.incident.dto;

import java.util.List;

public record AiLearningDto(
        String rootCause,
        String patternIdentified,
        List<String> preventionRecommendations,
        boolean knowledgeBaseEntryCreated
) {}
