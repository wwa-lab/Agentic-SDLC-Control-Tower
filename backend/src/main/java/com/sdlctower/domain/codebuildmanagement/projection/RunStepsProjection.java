package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.LogExcerptDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunStepRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StepConclusion;
import com.sdlctower.domain.codebuildmanagement.persistence.LogExcerptRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineJobEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineJobRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineStepEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineStepRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRunStepsProjection")
public class RunStepsProjection {

    private final PipelineJobRepository pipelineJobRepository;
    private final PipelineStepRepository pipelineStepRepository;
    private final LogExcerptRepository logExcerptRepository;

    public RunStepsProjection(
            PipelineJobRepository pipelineJobRepository,
            PipelineStepRepository pipelineStepRepository,
            LogExcerptRepository logExcerptRepository
    ) {
        this.pipelineJobRepository = pipelineJobRepository;
        this.pipelineStepRepository = pipelineStepRepository;
        this.logExcerptRepository = logExcerptRepository;
    }

    public List<RunStepRowDto> load(String runId) {
        List<PipelineJobEntity> jobs = pipelineJobRepository
                .findByRunIdOrderByJobNumberAsc(runId);

        return jobs.stream()
                .flatMap(job -> pipelineStepRepository
                        .findByJobIdOrderByOrderIndexAsc(job.getId())
                        .stream())
                .map(this::toStepDto)
                .toList();
    }

    private RunStepRowDto toStepDto(PipelineStepEntity step) {
        StepConclusion conclusion = step.getConclusion() != null
                ? CodeBuildManagementEnums.parse(StepConclusion.class, step.getConclusion(), "conclusion")
                : null;

        LogExcerptDto logExcerpt = isFailingStep(conclusion)
                ? resolveLogExcerpt(step)
                : null;

        return new RunStepRowDto(
                step.getId(),
                step.getName(),
                step.getOrderIndex(),
                conclusion,
                step.getStartedAt(),
                step.getCompletedAt(),
                logExcerpt
        );
    }

    private boolean isFailingStep(StepConclusion conclusion) {
        return conclusion == StepConclusion.FAILURE || conclusion == StepConclusion.TIMED_OUT;
    }

    private LogExcerptDto resolveLogExcerpt(PipelineStepEntity step) {
        return logExcerptRepository.findByStepId(step.getId())
                .map(log -> new LogExcerptDto(
                        log.getText(),
                        log.getByteCount(),
                        log.getExternalUrl()
                ))
                .orElse(null);
    }
}
