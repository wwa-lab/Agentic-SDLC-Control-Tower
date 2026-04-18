package com.sdlctower.domain.reportcenter.repository;

import com.sdlctower.domain.reportcenter.entity.ThroughputFactEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThroughputFactRepository extends JpaRepository<ThroughputFactEntity, Long> {

    List<ThroughputFactEntity> findByWeekStartBetween(LocalDate start, LocalDate end);
}
