package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.ReleaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReleaseRepository extends JpaRepository<ReleaseEntity, String> {
    Optional<ReleaseEntity> findByApplicationIdAndReleaseVersion(String applicationId, String releaseVersion);
    List<ReleaseEntity> findTop20ByApplicationIdOrderByCreatedAtDesc(String applicationId);
    List<ReleaseEntity> findByApplicationId(String applicationId);
}
