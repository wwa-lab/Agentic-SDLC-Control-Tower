package com.sdlctower.domain.teamspace.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricSnapshotRepository extends JpaRepository<MetricSnapshotEntity, String> {

    List<MetricSnapshotEntity> findByWorkspaceIdOrderBySnapshotAtDesc(String workspaceId);
}
