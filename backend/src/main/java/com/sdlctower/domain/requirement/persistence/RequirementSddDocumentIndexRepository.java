package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementSddDocumentIndexRepository extends JpaRepository<RequirementSddDocumentIndexEntity, String> {
    List<RequirementSddDocumentIndexEntity> findByRequirementIdOrderByIndexedAtAsc(String requirementId);
}
