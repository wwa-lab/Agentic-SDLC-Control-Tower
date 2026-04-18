package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPrReviewNoteRepository extends JpaRepository<AiPrReviewNoteEntity, String> {

    List<AiPrReviewNoteEntity> findByReviewId(String reviewId);
}
