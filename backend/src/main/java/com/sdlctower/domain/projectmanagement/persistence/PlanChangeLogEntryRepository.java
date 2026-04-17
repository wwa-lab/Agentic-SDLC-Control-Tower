package com.sdlctower.domain.projectmanagement.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanChangeLogEntryRepository extends JpaRepository<PlanChangeLogEntryEntity, String> {

    Page<PlanChangeLogEntryEntity> findByProjectIdOrderByCreatedAtDesc(String projectId, Pageable pageable);

    List<PlanChangeLogEntryEntity> findByProjectIdOrderByCreatedAtDesc(String projectId);
}
