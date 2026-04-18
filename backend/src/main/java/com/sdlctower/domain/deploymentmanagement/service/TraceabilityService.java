package com.sdlctower.domain.deploymentmanagement.service;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.*;
import com.sdlctower.shared.dto.SectionResultDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("deploymentTraceabilityService")
public class TraceabilityService {

    public TraceabilityAggregateDto inverseLookup(String storyId) {
        var chip = new StoryChipDto(storyId, "UNKNOWN_STORY", null, null);
        return new TraceabilityAggregateDto(
                chip,
                SectionResultDto.ok(List.of()),
                SectionResultDto.ok(List.of()),
                false);
    }
}
