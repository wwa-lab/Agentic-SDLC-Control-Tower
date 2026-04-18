package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.LogExcerptDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunJobRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunStepRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StepConclusion;
import com.sdlctower.domain.codebuildmanagement.persistence.LogExcerptEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.LogExcerptRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineJobEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineJobRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineStepEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineStepRepository;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRunJobsProjection")
public class RunJobsProjection {

    private final PipelineJobRepository pipelineJobRepository;
    private final PipelineStepRepository pipelineStepRepository;
    private final LogExcerptRepository logExcerptRepository;

    public RunJobsProjection(
            PipelineJobRepository pipelineJobRepository,
            PipelineStepRepository pipelineStepRepository,
            LogExcerptRepository logExcerptRepository
    ) {
        this.pipelineJobRepository = pipelineJobRepository;
        this.pipelineStepRepository = pipelineStepRepository;
        this.logExcerptRepository = logExcerptRepository;
    }

    public List<RunJobRowDto> load(String runId) {
        List<PipelineJobEntity> jobs = pipelineJobRepository
                .findByRunIdOrderByJobNumberAsc(runId);

        return jobs.stream()
                .map(this::toJobDto)
                .toList();
    }

    private RunJobRowDto toJobDto(PipelineJobEntity job) {
        List<PipelineStepEntity> stepEntities = pipelineStepRepository
                .findByJobIdOrderByOrderIndexAsc(job.getId());

        List<RunStepRowDto> steps = stepEntities.stream()
                .map(this::toStepDto)
                .toList();

        Integer durationSec = computeDuration(job);

        return new RunJobRowDto(
                job.getId(),
                job.getName(),
                job.getStatus() != null
                        ? CodeBuildManagementEnums.parse(RunStatus.class, job.getStatus(), "status")
                        : RunStatus.NEUTRAL,
                job.getConclusion() != null
                        ? CodeBuildManagementEnums.parse(StepConclusion.class, job.getConclusion(), "conclusion")
                        : null,
                durationSec,
                job.getStartedAt(),
                job.getCompletedAt(),
                steps
        );
    }

    private RunStepRowDto toStepDto(PipelineStepEntity step) {
        LogExcerptDto logExcerpt = resolveLogExcerpt(step);

        return new RunStepRowDto(
                step.getId(),
                step.getName(),
                step.getOrderIndex(),
                step.getConclusion() != null
                        ? CodeBuildManagementEnums.parse(StepConclusion.class, step.getConclusion(), "conclusion")
                        : null,
                step.getStartedAt(),
                step.getCompletedAt(),
                logExcerpt
        );
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

    private Integer computeDuration(PipelineJobEntity job) {
        if (job.getStartedAt() == null || job.getCompletedAt() == null) {
            return null;
        }
        return (int) Duration.between(job.getStartedAt(), job.getCompletedAt()).getSeconds();
    }
}
