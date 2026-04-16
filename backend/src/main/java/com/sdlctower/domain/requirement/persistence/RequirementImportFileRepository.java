package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementImportFileRepository extends JpaRepository<RequirementImportFileEntity, Long> {

    List<RequirementImportFileEntity> findByImportIdOrderByIdAsc(String importId);
}
