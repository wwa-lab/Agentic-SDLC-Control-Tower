package com.sdlctower.domain.requirement.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStoryRepository extends JpaRepository<UserStoryEntity, String> {

    List<UserStoryEntity> findByRequirementIdOrderByDisplayOrderAsc(String requirementId);

    List<UserStoryEntity> findByRequirementIdAndIdIn(String requirementId, List<String> ids);

    List<UserStoryEntity> findByRequirementIdIn(Collection<String> requirementIds);
}
