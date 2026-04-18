package com.sdlctower.domain.aicenter.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceLinkRepository extends JpaRepository<EvidenceLinkEntity, String> {

    List<EvidenceLinkEntity> findByExecutionIdOrderByPositionAsc(String executionId);
}
