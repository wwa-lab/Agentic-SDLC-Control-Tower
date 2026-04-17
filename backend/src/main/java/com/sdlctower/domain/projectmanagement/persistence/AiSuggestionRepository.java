package com.sdlctower.domain.projectmanagement.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiSuggestionRepository extends JpaRepository<AiSuggestionEntity, String> {

    List<AiSuggestionEntity> findByProjectIdAndStateOrderByCreatedAtDesc(String projectId, String state);

    List<AiSuggestionEntity> findByProjectIdOrderByCreatedAtDesc(String projectId);

    Optional<AiSuggestionEntity> findByProjectIdAndId(String projectId, String id);

    List<AiSuggestionEntity> findByProjectIdAndTargetTypeAndTargetIdAndSuppressUntilAfter(
            String projectId,
            String targetType,
            String targetId,
            Instant suppressUntil
    );
}
