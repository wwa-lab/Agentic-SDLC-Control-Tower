package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPrReviewRepository extends JpaRepository<AiPrReviewEntity, String> {

    Optional<AiPrReviewEntity> findTopByPrIdAndHeadShaOrderByGeneratedAtDesc(String prId, String headSha);

    List<AiPrReviewEntity> findByPrIdOrderByGeneratedAtDesc(String prId);
}
