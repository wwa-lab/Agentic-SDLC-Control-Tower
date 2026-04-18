package com.sdlctower.domain.reportcenter.repository;

import com.sdlctower.domain.reportcenter.entity.FlowEfficiencyFactEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowEfficiencyFactRepository extends JpaRepository<FlowEfficiencyFactEntity, Long> {

    List<FlowEfficiencyFactEntity> findByBucketDateBetween(LocalDate start, LocalDate end);
}
