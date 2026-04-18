package com.sdlctower.domain.reportcenter.repository;

import com.sdlctower.domain.reportcenter.entity.WipFactEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WipFactRepository extends JpaRepository<WipFactEntity, Long> {

    List<WipFactEntity> findBySnapshotAtBetween(Instant start, Instant end);
}
