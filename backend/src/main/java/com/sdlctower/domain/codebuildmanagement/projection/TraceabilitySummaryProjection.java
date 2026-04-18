package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityCommitRefDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilitySummaryDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StoryLinkStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementTraceabilitySummaryProjection")
public class TraceabilitySummaryProjection {

    private final CommitStoryLinkRepository commitStoryLinkRepository;

    public TraceabilitySummaryProjection(CommitStoryLinkRepository commitStoryLinkRepository) {
        this.commitStoryLinkRepository = commitStoryLinkRepository;
    }

    public TraceabilitySummaryDto load(String workspaceId) {
        List<CommitStoryLinkEntity> allLinks = commitStoryLinkRepository.findAll();

        int knownCount = countByStatus(allLinks, "KNOWN");
        int unknownStoryCount = countByStatus(allLinks, "UNKNOWN_STORY");
        int noStoryIdCount = countByStatus(allLinks, "NO_STORY_ID");
        int ambiguousCount = countByStatus(allLinks, "AMBIGUOUS");

        return new TraceabilitySummaryDto(
                workspaceId,
                knownCount,
                unknownStoryCount,
                noStoryIdCount,
                ambiguousCount
        );
    }

    private int countByStatus(List<CommitStoryLinkEntity> links, String status) {
        return (int) links.stream()
                .filter(link -> status.equals(link.getLinkStatus()))
                .count();
    }
}
