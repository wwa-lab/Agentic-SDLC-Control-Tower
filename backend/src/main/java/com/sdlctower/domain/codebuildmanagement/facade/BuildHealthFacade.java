package com.sdlctower.domain.codebuildmanagement.facade;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Cross-slice read facade consumed by Dashboard and Project Management.
 * Provides build health metrics without exposing Code & Build Management internals.
 */
@Component("codeBuildManagementBuildHealthFacade")
public class BuildHealthFacade {

    private final RepoRepository repoRepository;
    private final PipelineRunRepository pipelineRunRepository;

    public BuildHealthFacade(RepoRepository repoRepository, PipelineRunRepository pipelineRunRepository) {
        this.repoRepository = repoRepository;
        this.pipelineRunRepository = pipelineRunRepository;
    }

    public record ProjectHealth(
            double successRate14d,
            double medianDurationSec,
            String lastRunStatus,
            List<String> failingWorkflows
    ) {}

    public record WorkspaceHealth(
            int totalRepos,
            int reposWithRecentFailure,
            double overallSuccessRate14d
    ) {}

    public record RecentFailure(
            String repoFullName,
            String workflowName,
            String runId,
            Instant failedAt
    ) {}

    public ProjectHealth healthByProject(String projectId) {
        List<RepoEntity> repos = repoRepository.findByProjectId(projectId);
        if (repos.isEmpty()) {
            return new ProjectHealth(0.0, 0.0, "NONE", List.of());
        }

        List<PipelineRunEntity> recentRuns = repos.stream()
                .flatMap(repo -> pipelineRunRepository.findTop25ByRepoIdOrderByCreatedAtDesc(repo.getId()).stream())
                .toList();

        long successCount = recentRuns.stream()
                .filter(r -> "SUCCESS".equals(r.getStatus()))
                .count();
        long failureCount = recentRuns.stream()
                .filter(r -> "FAILURE".equals(r.getStatus()))
                .count();
        double successRate = (successCount + failureCount) == 0 ? 0.0
                : (double) successCount / (successCount + failureCount);

        double medianDuration = recentRuns.stream()
                .filter(r -> r.getDurationSec() != null)
                .mapToInt(PipelineRunEntity::getDurationSec)
                .sorted()
                .skip(recentRuns.size() / 2)
                .findFirst()
                .orElse(0);

        String lastRunStatus = recentRuns.isEmpty() ? "NONE" : recentRuns.get(0).getStatus();

        List<String> failingWorkflows = recentRuns.stream()
                .filter(r -> "FAILURE".equals(r.getStatus()))
                .collect(Collectors.groupingBy(PipelineRunEntity::getPipelineName, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(java.util.Map.Entry::getKey)
                .toList();

        return new ProjectHealth(successRate, medianDuration, lastRunStatus, failingWorkflows);
    }

    public WorkspaceHealth healthByWorkspace(String workspaceId) {
        List<RepoEntity> repos = repoRepository.findByWorkspaceId(workspaceId);
        int reposWithFailure = 0;
        long totalSuccess = 0;
        long totalFailure = 0;

        for (RepoEntity repo : repos) {
            List<PipelineRunEntity> runs = pipelineRunRepository.findTop25ByRepoIdOrderByCreatedAtDesc(repo.getId());
            boolean hasFailure = runs.stream().anyMatch(r -> "FAILURE".equals(r.getStatus()));
            if (hasFailure) reposWithFailure++;
            totalSuccess += runs.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count();
            totalFailure += runs.stream().filter(r -> "FAILURE".equals(r.getStatus())).count();
        }

        double successRate = (totalSuccess + totalFailure) == 0 ? 0.0
                : (double) totalSuccess / (totalSuccess + totalFailure);

        return new WorkspaceHealth(repos.size(), reposWithFailure, successRate);
    }

    public List<RecentFailure> recentFailuresByWorkspace(String workspaceId, Instant since) {
        List<RepoEntity> repos = repoRepository.findByWorkspaceId(workspaceId);
        return repos.stream()
                .flatMap(repo -> pipelineRunRepository.findTop25ByRepoIdOrderByCreatedAtDesc(repo.getId()).stream()
                        .filter(r -> "FAILURE".equals(r.getStatus()))
                        .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(since))
                        .map(r -> new RecentFailure(repo.getFullName(), r.getPipelineName(), r.getId(), r.getCreatedAt())))
                .toList();
    }
}
