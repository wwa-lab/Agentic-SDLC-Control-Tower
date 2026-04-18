package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityCommitRefDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityStoryRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StoryLinkStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementTraceabilityStoryRowsProjection")
public class TraceabilityStoryRowsProjection {

    private final CommitStoryLinkRepository commitStoryLinkRepository;
    private final GitCommitRepository gitCommitRepository;
    private final RepoRepository repoRepository;
    private final PipelineRunRepository pipelineRunRepository;
    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;

    public TraceabilityStoryRowsProjection(
            CommitStoryLinkRepository commitStoryLinkRepository,
            GitCommitRepository gitCommitRepository,
            RepoRepository repoRepository,
            PipelineRunRepository pipelineRunRepository,
            ProjectSpaceSeedCatalog projectSpaceSeedCatalog
    ) {
        this.commitStoryLinkRepository = commitStoryLinkRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.repoRepository = repoRepository;
        this.pipelineRunRepository = pipelineRunRepository;
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
    }

    public List<TraceabilityStoryRowDto> load(String workspaceId, String projectId) {
        List<CommitStoryLinkEntity> allLinks = commitStoryLinkRepository.findAll().stream()
                .filter(link -> "KNOWN".equals(link.getLinkStatus()))
                .filter(link -> link.getStoryId() != null)
                .toList();

        Map<String, List<CommitStoryLinkEntity>> linksByStory = new LinkedHashMap<>();
        for (CommitStoryLinkEntity link : allLinks) {
            linksByStory.computeIfAbsent(link.getStoryId(), key -> new ArrayList<>()).add(link);
        }

        return linksByStory.entrySet().stream()
                .map(entry -> buildStoryRow(entry.getKey(), entry.getValue(), workspaceId))
                .filter(row -> projectId == null || projectId.isBlank() || projectId.equals(row.projectId()))
                .toList();
    }

    private TraceabilityStoryRowDto buildStoryRow(
            String storyId,
            List<CommitStoryLinkEntity> links,
            String workspaceId
    ) {
        List<String> commitIds = links.stream()
                .map(CommitStoryLinkEntity::getCommitId)
                .toList();
        List<GitCommitEntity> commits = gitCommitRepository.findByIdIn(commitIds);

        List<TraceabilityCommitRefDto> commitRefs = commits.stream()
                .map(commit -> toCommitRef(commit, links))
                .toList();

        int linkedBuildCount = (int) commits.stream()
                .filter(commit -> resolveBuildRunId(commit) != null)
                .count();

        StoryLinkStatus worstLinkStatus = resolveWorstLinkStatus(links);
        String projId = resolveProjectIdFromCommits(commits);

        return new TraceabilityStoryRowDto(
                storyId,
                null,
                projId,
                resolveProjectName(projId),
                null,
                commits.size(),
                linkedBuildCount,
                worstLinkStatus,
                commitRefs
        );
    }

    private TraceabilityCommitRefDto toCommitRef(
            GitCommitEntity commit,
            List<CommitStoryLinkEntity> links
    ) {
        String repoFullName = resolveRepoFullName(commit.getRepoId());
        String buildRunId = resolveBuildRunId(commit);
        RunStatus buildStatus = resolveBuildStatus(commit);

        StoryLinkStatus linkStatus = links.stream()
                .filter(link -> link.getCommitId().equals(commit.getId()))
                .findFirst()
                .map(link -> CodeBuildManagementEnums.parse(
                        StoryLinkStatus.class, link.getLinkStatus(), "linkStatus"))
                .orElse(StoryLinkStatus.KNOWN);

        return new TraceabilityCommitRefDto(
                commit.getSha(),
                commit.getShortSha(),
                commit.getAuthor(),
                commit.getMessage(),
                repoFullName,
                linkStatus,
                buildRunId,
                buildStatus,
                commit.getCommittedAt()
        );
    }

    private String resolveRepoFullName(String repoId) {
        return repoRepository.findById(repoId)
                .map(RepoEntity::getFullName)
                .orElse(repoId);
    }

    private String resolveBuildRunId(GitCommitEntity commit) {
        if (commit.getSha() == null) {
            return null;
        }
        return pipelineRunRepository.findByRepoIdAndHeadSha(commit.getRepoId(), commit.getSha())
                .map(PipelineRunEntity::getId)
                .orElse(null);
    }

    private RunStatus resolveBuildStatus(GitCommitEntity commit) {
        if (commit.getSha() == null) {
            return null;
        }
        return pipelineRunRepository.findByRepoIdAndHeadSha(commit.getRepoId(), commit.getSha())
                .map(run -> parseRunStatus(run.getStatus()))
                .orElse(null);
    }

    private RunStatus parseRunStatus(String status) {
        if (status == null || status.isBlank()) {
            return RunStatus.NEUTRAL;
        }
        try {
            return RunStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return RunStatus.NEUTRAL;
        }
    }

    private StoryLinkStatus resolveWorstLinkStatus(List<CommitStoryLinkEntity> links) {
        return links.stream()
                .map(link -> CodeBuildManagementEnums.parse(
                        StoryLinkStatus.class, link.getLinkStatus(), "linkStatus"))
                .min(Comparator.comparingInt(this::linkStatusSeverity))
                .orElse(StoryLinkStatus.KNOWN);
    }

    private int linkStatusSeverity(StoryLinkStatus status) {
        return switch (status) {
            case NO_STORY_ID -> 0;
            case UNKNOWN_STORY -> 1;
            case AMBIGUOUS -> 2;
            case KNOWN -> 3;
        };
    }

    private String resolveProjectIdFromCommits(List<GitCommitEntity> commits) {
        return commits.stream()
                .map(commit -> repoRepository.findById(commit.getRepoId())
                        .map(RepoEntity::getProjectId)
                        .orElse(null))
                .filter(id -> id != null)
                .findFirst()
                .orElse(null);
    }

    private String resolveProjectName(String projectId) {
        if (projectId == null) {
            return "Unknown project";
        }
        try {
            return projectSpaceSeedCatalog.project(projectId).name();
        } catch (Exception ex) {
            return projectId;
        }
    }
}
