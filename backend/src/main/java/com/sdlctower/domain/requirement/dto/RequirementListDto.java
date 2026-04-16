package com.sdlctower.domain.requirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RequirementListDto(
        StatusDistributionDto statusDistribution,
        List<RequirementListItemDto> items,
        int totalCount
) {

    @JsonProperty("requirements")
    public List<RequirementListItemDto> requirements() {
        return items;
    }
}
