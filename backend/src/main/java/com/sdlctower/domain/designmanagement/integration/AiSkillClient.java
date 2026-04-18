package com.sdlctower.domain.designmanagement.integration;

import com.sdlctower.shared.integration.requirement.SpecRevisionLookup.SpecRevisionInfo;
import java.util.List;

public interface AiSkillClient {

    AiSummaryResult summarize(String artifactTitle, String htmlPayload, List<SpecRevisionInfo> linkedSpecs, String skillVersion);

    record AiSummaryResult(
            String skillVersion,
            String summaryText,
            List<String> keyElements
    ) {}
}
