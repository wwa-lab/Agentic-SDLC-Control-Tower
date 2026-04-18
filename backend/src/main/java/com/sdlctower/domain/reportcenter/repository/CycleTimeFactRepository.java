package com.sdlctower.domain.reportcenter.repository;

import com.sdlctower.domain.reportcenter.entity.CycleTimeFactEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CycleTimeFactRepository extends JpaRepository<CycleTimeFactEntity, Long> {

    List<CycleTimeFactEntity> findByBucketDateBetween(LocalDate start, LocalDate end);
}
