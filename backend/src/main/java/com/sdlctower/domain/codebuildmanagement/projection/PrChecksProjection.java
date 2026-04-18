package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrCheckRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StepConclusion;
import com.sdlctower.domain.codebuildmanagement.persistence.CheckRunEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.CheckRunRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestRepository;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementPrChecksProjection")
public class PrChecksProjection {

    private final PullRequestRepository pullRequestRepository;
    private final CheckRunRepository checkRunRepository;
    private final PipelineRunRepository pipelineRunRepository;

    public PrChecksProjection(
            PullRequestRepository pullRequestRepository,
            CheckRunRepository checkRunRepository,
            PipelineRunRepository pipelineRunRepository
    ) {
        this.pullRequestRepository = pullRequestRepository;
        this.checkRunRepository = checkRunRepository;
        this.pipelineRunRepository = pipelineRunRepository;
    }

    public List<PrCheckRowDto> load(String prId) {
        PullRequestEntity pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_PR_NOT_FOUND", "Pull request not found: " + prId));

        if (pr.getHeadSha() == null) {
            return List.of();
        }

        List<CheckRunEntity> checkRuns = checkRunRepository
                .findByRepoIdAndHeadSha(pr.getRepoId(), pr.getHeadSha());

        return checkRuns.stream()
                .map(check -> toDto(check, pr.getRepoId()))
                .toList();
    }

    private PrCheckRowDto toDto(CheckRunEntity check, String repoId) {
        Integer durationSec = computeDuration(check);
        String runId = resolveRunId(repoId, check.getHeadSha());

        return new PrCheckRowDto(
                check.getName(),
                check.getStatus() != null
                        ? CodeBuildManagementEnums.parse(RunStatus.class, check.getStatus(), "status")
                        : RunStatus.NEUTRAL,
                check.getConclusion() != null
                        ? CodeBuildManagementEnums.parse(StepConclusion.class, check.getConclusion(), "conclusion")
                        : null,
                durationSec,
                runId,
                check.getExternalUrl()
        );
    }

    private Integer computeDuration(CheckRunEntity check) {
        if (check.getStartedAt() == null || check.getCompletedAt() == null) {
            return null;
        }
        return (int) Duration.between(check.getStartedAt(), check.getCompletedAt()).getSeconds();
    }

    private String resolveRunId(String repoId, String headSha) {
        return pipelineRunRepository.findByRepoIdAndHeadSha(repoId, headSha)
                .map(run -> run.getId())
                .orElse(null);
    }
}
