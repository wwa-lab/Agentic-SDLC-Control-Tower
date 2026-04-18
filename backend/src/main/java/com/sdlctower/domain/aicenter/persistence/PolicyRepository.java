package com.sdlctower.domain.aicenter.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<PolicyEntity, String> {

    Optional<PolicyEntity> findByWorkspaceIdAndSkillId(String workspaceId, String skillId);
}
