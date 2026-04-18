package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoRecentRunRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunTrigger;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRepoRecentRunsProjection")
public class RepoRecentRunsProjection {

    private final PipelineRunRepository pipelineRunRepository;

    public RepoRecentRunsProjection(PipelineRunRepository pipelineRunRepository) {
        this.pipelineRunRepository = pipelineRunRepository;
    }

    public List<RepoRecentRunRowDto> load(String repoId) {
        List<PipelineRunEntity> runs = pipelineRunRepository
                .findTop25ByRepoIdOrderByCreatedAtDesc(repoId);

        return runs.stream()
                .map(this::toDto)
                .toList();
    }

    private RepoRecentRunRowDto toDto(PipelineRunEntity run) {
        return new RepoRecentRunRowDto(
                run.getId(),
                run.getRunNumber(),
                run.getPipelineName(),
                CodeBuildManagementEnums.parse(RunStatus.class, run.getStatus(), "status"),
                CodeBuildManagementEnums.parse(RunTrigger.class, run.getTrigger(), "trigger"),
                run.getBranch(),
                run.getHeadSha(),
                run.getActor(),
                run.getDurationSec(),
                run.getCreatedAt()
        );
    }
}
