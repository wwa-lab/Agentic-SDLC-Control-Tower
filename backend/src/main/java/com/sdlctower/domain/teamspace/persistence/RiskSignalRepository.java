package com.sdlctower.domain.teamspace.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskSignalRepository extends JpaRepository<RiskSignalEntity, String> {

    List<RiskSignalEntity> findByWorkspaceIdOrderByDetectedAtDesc(String workspaceId);

    List<RiskSignalEntity> findByWorkspaceIdAndResolvedAtIsNullOrderByDetectedAtDesc(String workspaceId);

    List<RiskSignalEntity> findByWorkspaceIdAndProjectIdIsNullOrderByDetectedAtDesc(String workspaceId);

    List<RiskSignalEntity> findByWorkspaceIdAndProjectIdIsNullAndResolvedAtIsNullOrderByDetectedAtDesc(String workspaceId);

    List<RiskSignalEntity> findByProjectIdAndResolvedAtIsNullOrderByDetectedAtDesc(String projectId);
}
