package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoBranchDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.BranchEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.BranchRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRepoBranchesProjection")
public class RepoBranchesProjection {

    private final BranchRepository branchRepository;
    private final PipelineRunRepository pipelineRunRepository;
    private final RepoRepository repoRepository;

    public RepoBranchesProjection(
            BranchRepository branchRepository,
            PipelineRunRepository pipelineRunRepository,
            RepoRepository repoRepository
    ) {
        this.branchRepository = branchRepository;
        this.pipelineRunRepository = pipelineRunRepository;
        this.repoRepository = repoRepository;
    }

    public List<RepoBranchDto> load(String repoId) {
        RepoEntity repo = repoRepository.findById(repoId)
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_REPO_NOT_FOUND", "Repo not found: " + repoId));

        List<BranchEntity> branches = branchRepository.findByRepoIdOrderByNameAsc(repoId);

        return branches.stream()
                .map(branch -> toBranchDto(branch, repo.getDefaultBranch()))
                .toList();
    }

    private RepoBranchDto toBranchDto(BranchEntity branch, String defaultBranch) {
        RunStatus lastRunStatus = resolveLastRunStatus(branch);

        return new RepoBranchDto(
                branch.getName(),
                branch.getName().equals(defaultBranch),
                branch.getHeadSha(),
                lastRunStatus,
                branch.getLastCommitAt()
        );
    }

    private RunStatus resolveLastRunStatus(BranchEntity branch) {
        if (branch.getHeadSha() == null) {
            return null;
        }
        return pipelineRunRepository.findByRepoIdAndHeadSha(branch.getRepoId(), branch.getHeadSha())
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
}
