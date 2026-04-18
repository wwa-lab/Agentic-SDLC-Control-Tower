package com.sdlctower.domain.testingmanagement.service;

import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.testingmanagement.TestingManagementConstants;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.MemberRefDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.ReqLinkStatus;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup.RequirementRef;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component("testingManagementReadSupport")
public class TestingManagementReadSupport {

    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;
    private final TeamSpaceSeedCatalog teamSpaceSeedCatalog;

    public TestingManagementReadSupport(
            ProjectSpaceSeedCatalog projectSpaceSeedCatalog,
            TeamSpaceSeedCatalog teamSpaceSeedCatalog
    ) {
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
        this.teamSpaceSeedCatalog = teamSpaceSeedCatalog;
    }

    public MemberRefDto memberRef(String memberId) {
        return new MemberRefDto(memberId, projectSpaceSeedCatalog.memberDisplayName(memberId));
    }

    public String projectName(String projectId) {
        return projectSpaceSeedCatalog.project(projectId).name();
    }

    public String workspaceName(String workspaceId) {
        return teamSpaceSeedCatalog.workspace(workspaceId).name();
    }

    public CoverageStatus resolveChipColor(ReqLinkStatus linkStatus, RequirementRef requirementRef) {
        if (linkStatus == ReqLinkStatus.UNVERIFIED) {
            return CoverageStatus.GREY;
        }
        if (requirementRef == null) {
            return CoverageStatus.RED;
        }
        String state = requirementRef.state() == null ? "" : requirementRef.state().trim().toLowerCase(Locale.ROOT);
        if (state.contains("delivered") || state.contains("approved") || state.contains("done") || state.contains("implemented")) {
            return CoverageStatus.GREEN;
        }
        return CoverageStatus.AMBER;
    }

    public String sanitizeMarkdown(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return markdown;
        }
        return markdown.replaceAll("(?is)<[^>]+>", "").trim();
    }

    public String truncateFailureExcerpt(String excerpt) {
        if (excerpt == null || excerpt.length() <= TestingManagementConstants.FAILURE_EXCERPT_LIMIT) {
            return excerpt;
        }
        return excerpt.substring(0, TestingManagementConstants.FAILURE_EXCERPT_LIMIT);
    }
}
