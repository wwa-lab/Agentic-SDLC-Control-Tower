package com.sdlctower.domain.reportcenter.repository;

import com.sdlctower.domain.reportcenter.entity.LeadTimeFactEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadTimeFactRepository extends JpaRepository<LeadTimeFactEntity, Long> {

    List<LeadTimeFactEntity> findByBucketDateBetween(LocalDate start, LocalDate end);
}
