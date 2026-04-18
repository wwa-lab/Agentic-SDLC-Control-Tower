package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiTriageRowRepository extends JpaRepository<AiTriageRowEntity, String> {

    List<AiTriageRowEntity> findByRunIdAndSkillVersion(String runId, String skillVersion);

    List<AiTriageRowEntity> findByRunIdOrderByGeneratedAtDesc(String runId);
}
