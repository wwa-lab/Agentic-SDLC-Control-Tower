package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogExcerptRepository extends JpaRepository<LogExcerptEntity, String> {

    Optional<LogExcerptEntity> findByStepId(String stepId);
}
