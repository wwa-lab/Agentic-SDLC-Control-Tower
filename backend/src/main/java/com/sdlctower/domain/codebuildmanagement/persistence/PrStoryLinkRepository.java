package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrStoryLinkRepository extends JpaRepository<PrStoryLinkEntity, String> {

    List<PrStoryLinkEntity> findByPrId(String prId);

    List<PrStoryLinkEntity> findByStoryIdIn(Collection<String> storyIds);
}
