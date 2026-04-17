package com.sdlctower.domain.projectmanagement.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapacityAllocationRepository extends JpaRepository<CapacityAllocationEntity, String> {

    List<CapacityAllocationEntity> findByProjectIdOrderByWindowStartAscMemberIdAsc(String projectId);

    List<CapacityAllocationEntity> findByProjectIdAndWindowStartBetween(String projectId, LocalDate from, LocalDate to);

    Optional<CapacityAllocationEntity> findByProjectIdAndMemberIdAndMilestoneId(
            String projectId,
            String memberId,
            String milestoneId
    );
}
