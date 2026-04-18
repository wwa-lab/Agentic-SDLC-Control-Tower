package com.sdlctower.domain.codebuildmanagement.policy;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StoryLinkStatus;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementStoryIdExtractor")
public class StoryIdExtractor {

    private static final Pattern TRAILER = Pattern.compile(
            "^\\s*(?:Story-Id|Relates-to)\\s*:\\s*(STORY-[A-Z0-9\\-]+)\\s*$",
            Pattern.MULTILINE);

    public record ExtractionResult(List<String> storyIds, StoryLinkStatus status) {}

    public ExtractionResult extract(String text) {
        if (text == null || text.isBlank()) {
            return new ExtractionResult(List.of(), StoryLinkStatus.NO_STORY_ID);
        }

        Matcher matcher = TRAILER.matcher(text);
        Set<String> distinctIds = new LinkedHashSet<>();
        while (matcher.find()) {
            distinctIds.add(matcher.group(1));
        }

        if (distinctIds.isEmpty()) {
            return new ExtractionResult(List.of(), StoryLinkStatus.NO_STORY_ID);
        }

        List<String> ids = new ArrayList<>(distinctIds);

        if (distinctIds.size() == 1) {
            return new ExtractionResult(ids, StoryLinkStatus.KNOWN);
        }

        return new ExtractionResult(ids, StoryLinkStatus.AMBIGUOUS);
    }
}
