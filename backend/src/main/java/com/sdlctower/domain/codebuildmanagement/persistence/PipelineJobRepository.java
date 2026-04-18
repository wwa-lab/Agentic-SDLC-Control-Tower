package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineJobRepository extends JpaRepository<PipelineJobEntity, String> {

    List<PipelineJobEntity> findByRunIdOrderByJobNumberAsc(String runId);
}
