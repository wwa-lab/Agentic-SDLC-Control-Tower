package com.sdlctower.domain.projectspace.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilestoneRepository extends JpaRepository<MilestoneEntity, String> {

    List<MilestoneEntity> findByProjectIdOrderByOrderingAsc(String projectId);

    List<MilestoneEntity> findByProjectIdOrderByTargetDateAsc(String projectId);

    Optional<MilestoneEntity> findFirstByProjectIdAndIsCurrentTrueOrderByOrderingAsc(String projectId);

    Optional<MilestoneEntity> findByProjectIdAndId(String projectId, String id);
}
