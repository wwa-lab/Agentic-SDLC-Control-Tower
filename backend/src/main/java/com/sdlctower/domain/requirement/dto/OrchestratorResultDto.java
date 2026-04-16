package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrchestratorResultDto(
        String determinedPathId,
        String determinedTier,
        String confidence,
        String reasoning
) {

    @JsonProperty("determinedTierId")
    public String determinedTierId() {
        return determinedTier;
    }
}
