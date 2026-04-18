package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullRequestReviewRepository extends JpaRepository<PullRequestReviewEntity, String> {

    List<PullRequestReviewEntity> findByPrIdOrderBySubmittedAtAsc(String prId);
}
