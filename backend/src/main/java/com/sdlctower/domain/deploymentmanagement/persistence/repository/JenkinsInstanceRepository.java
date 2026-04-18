package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.JenkinsInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JenkinsInstanceRepository extends JpaRepository<JenkinsInstanceEntity, String> {
    List<JenkinsInstanceEntity> findByWorkspaceId(String workspaceId);
}
