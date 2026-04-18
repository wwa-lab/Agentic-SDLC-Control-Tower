package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineStepRepository extends JpaRepository<PipelineStepEntity, String> {

    List<PipelineStepEntity> findByJobIdOrderByOrderIndexAsc(String jobId);
}
