package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineRunRepository extends JpaRepository<PipelineRunEntity, String> {

    List<PipelineRunEntity> findTop25ByRepoIdOrderByCreatedAtDesc(String repoId);

    Optional<PipelineRunEntity> findByRepoIdAndHeadSha(String repoId, String headSha);

    Optional<PipelineRunEntity> findByGithubRunId(Long githubRunId);
}
