package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.AiReleaseNotesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AiReleaseNotesRepository extends JpaRepository<AiReleaseNotesEntity, String> {
    Optional<AiReleaseNotesEntity> findTopByReleaseIdOrderByGeneratedAtDesc(String releaseId);
}
