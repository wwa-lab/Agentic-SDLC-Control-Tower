package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequirementDraftDto(
        String title,
        String priority,
        String category,
        String summary,
        String businessJustification,
        List<String> acceptanceCriteria,
        List<String> assumptions,
        List<String> constraints,
        List<String> missingInfo,
        List<String> openQuestions,
        List<String> aiSuggestedFields,
        String normalizedBy,
        String normalizedAt,
        ImportInspectionDto importInspection
) {

    @JsonProperty("prioritySuggestion")
    public String prioritySuggestion() {
        return priority;
    }

    @JsonProperty("categorySuggestion")
    public String categorySuggestion() {
        return category;
    }

    @JsonProperty("description")
    public String description() {
        return summary;
    }
}
