package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoRepository extends JpaRepository<RepoEntity, String> {

    Page<RepoEntity> findByWorkspaceIdIn(Collection<String> workspaceIds, Pageable pageable);

    List<RepoEntity> findByWorkspaceIdIn(Collection<String> workspaceIds);

    List<RepoEntity> findByProjectId(String projectId);

    List<RepoEntity> findByWorkspaceId(String workspaceId);

    Optional<RepoEntity> findByWorkspaceIdAndProjectId(String workspaceId, String projectId);
}
