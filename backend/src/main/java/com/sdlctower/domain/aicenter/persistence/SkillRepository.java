package com.sdlctower.domain.aicenter.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<SkillEntity, String> {

    List<SkillEntity> findByWorkspaceIdOrderByNameAsc(String workspaceId);

    Optional<SkillEntity> findByWorkspaceIdAndKey(String workspaceId, String key);

    List<SkillEntity> findByWorkspaceIdAndCategory(String workspaceId, String category);

    long countByWorkspaceIdAndStatus(String workspaceId, String status);
}
