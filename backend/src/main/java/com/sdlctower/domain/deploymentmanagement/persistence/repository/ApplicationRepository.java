package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.ApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, String> {
    Page<ApplicationEntity> findByWorkspaceIdIn(Collection<String> workspaceIds, Pageable pageable);
    Optional<ApplicationEntity> findBySlugAndWorkspaceId(String slug, String workspaceId);
    java.util.List<ApplicationEntity> findByWorkspaceId(String workspaceId);
    java.util.List<ApplicationEntity> findByProjectId(String projectId);
}
