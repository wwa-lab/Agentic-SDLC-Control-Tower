package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionOutboxRepository extends JpaRepository<IngestionOutboxEntity, String> {

    Optional<IngestionOutboxEntity> findByDeliveryId(String deliveryId);

    Page<IngestionOutboxEntity> findByStatusOrderByReceivedAtAsc(String status, Pageable pageable);
}
