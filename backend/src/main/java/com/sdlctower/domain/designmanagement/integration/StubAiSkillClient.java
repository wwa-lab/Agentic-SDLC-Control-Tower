package com.sdlctower.domain.designmanagement.integration;

import com.sdlctower.shared.integration.requirement.SpecRevisionLookup.SpecRevisionInfo;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StubAiSkillClient implements AiSkillClient {

    @Override
    public AiSummaryResult summarize(String artifactTitle, String htmlPayload, List<SpecRevisionInfo> linkedSpecs, String skillVersion) {
        String plainText = htmlPayload
                .replaceAll("(?is)<script.*?>.*?</script>", " ")
                .replaceAll("(?is)<style.*?>.*?</style>", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
        String summary = plainText.isBlank()
                ? artifactTitle + " design mock is registered without readable body content."
                : artifactTitle + " mock focuses on " + abbreviate(plainText, 180);

        List<String> keyElements = new ArrayList<>(linkedSpecs.stream()
                .limit(5)
                .map(SpecRevisionInfo::title)
                .toList());
        if (keyElements.isEmpty()) {
            keyElements.add("No linked specs yet");
        }
        return new AiSummaryResult(skillVersion, summary, keyElements);
    }

    private String abbreviate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 1) + "…";
    }
}
