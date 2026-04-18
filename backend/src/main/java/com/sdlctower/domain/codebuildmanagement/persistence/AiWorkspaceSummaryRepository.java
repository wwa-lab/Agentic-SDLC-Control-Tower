package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiWorkspaceSummaryRepository extends JpaRepository<AiWorkspaceSummaryEntity, String> {

    Optional<AiWorkspaceSummaryEntity> findTopByWorkspaceIdAndRepoIdOrderByGeneratedAtDesc(
            String workspaceId, String repoId);
}
