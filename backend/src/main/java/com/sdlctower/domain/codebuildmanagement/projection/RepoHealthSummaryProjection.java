package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.CodeBuildManagementConstants;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.FailingWorkflowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoHealthSummaryDto;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRepoHealthSummaryProjection")
public class RepoHealthSummaryProjection {

    private final PipelineRunRepository pipelineRunRepository;

    public RepoHealthSummaryProjection(PipelineRunRepository pipelineRunRepository) {
        this.pipelineRunRepository = pipelineRunRepository;
    }

    public RepoHealthSummaryDto load(String repoId) {
        List<PipelineRunEntity> recentRuns = pipelineRunRepository
                .findTop25ByRepoIdOrderByCreatedAtDesc(repoId)
                .stream()
                .filter(run -> run.getCreatedAt() != null && run.getCreatedAt().isAfter(
                        CodeBuildManagementConstants.REFERENCE_NOW.minus(CodeBuildManagementConstants.RECENT_WINDOW)))
                .toList();

        double successRate14d = computeSuccessRate(recentRuns);
        double medianDurationSec = computeMedianDuration(recentRuns);
        List<FailingWorkflowDto> topFailingWorkflows = computeTopFailingWorkflows(recentRuns);

        return new RepoHealthSummaryDto(successRate14d, medianDurationSec, topFailingWorkflows);
    }

    private double computeSuccessRate(List<PipelineRunEntity> runs) {
        List<PipelineRunEntity> completed = runs.stream()
                .filter(run -> "SUCCESS".equals(run.getStatus()) || "FAILURE".equals(run.getStatus()))
                .toList();
        if (completed.isEmpty()) {
            return 0d;
        }
        long successCount = completed.stream()
                .filter(run -> "SUCCESS".equals(run.getStatus()))
                .count();
        return (double) successCount / (double) completed.size();
    }

    private double computeMedianDuration(List<PipelineRunEntity> runs) {
        List<Integer> durations = runs.stream()
                .filter(run -> run.getDurationSec() != null)
                .map(PipelineRunEntity::getDurationSec)
                .sorted()
                .toList();
        if (durations.isEmpty()) {
            return 0d;
        }
        int mid = durations.size() / 2;
        if (durations.size() % 2 == 0) {
            return (durations.get(mid - 1) + durations.get(mid)) / 2.0;
        }
        return durations.get(mid);
    }

    private List<FailingWorkflowDto> computeTopFailingWorkflows(List<PipelineRunEntity> runs) {
        Map<String, Long> failuresByWorkflow = runs.stream()
                .filter(run -> "FAILURE".equals(run.getStatus()))
                .filter(run -> run.getPipelineName() != null)
                .collect(Collectors.groupingBy(PipelineRunEntity::getPipelineName, Collectors.counting()));

        return failuresByWorkflow.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(entry -> new FailingWorkflowDto(entry.getKey(), entry.getValue().intValue()))
                .toList();
    }
}
