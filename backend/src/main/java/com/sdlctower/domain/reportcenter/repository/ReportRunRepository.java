package com.sdlctower.domain.reportcenter.repository;

import com.sdlctower.domain.reportcenter.entity.ReportRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRunRepository extends JpaRepository<ReportRun, String> {

    List<ReportRun> findTop50ByUserIdOrderByRunAtDesc(String userId);

    List<ReportRun> findTop50ByUserIdAndReportKeyOrderByRunAtDesc(String userId, String reportKey);
}
