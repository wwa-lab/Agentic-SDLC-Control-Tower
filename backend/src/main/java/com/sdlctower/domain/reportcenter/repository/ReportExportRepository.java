package com.sdlctower.domain.reportcenter.repository;

import com.sdlctower.domain.reportcenter.entity.ReportExport;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportExportRepository extends JpaRepository<ReportExport, String> {

    Optional<ReportExport> findByIdAndUserId(String id, String userId);

    List<ReportExport> findTop50ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(String userId, Instant createdAt);

    List<ReportExport> findByStatusAndExpiresAtBefore(String status, Instant expiresAt);
}
