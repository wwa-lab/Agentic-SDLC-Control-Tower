package com.sdlctower.domain.testingmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestPlanRepository extends JpaRepository<TestPlanEntity, String> {

    List<TestPlanEntity> findByWorkspaceIdOrderByUpdatedAtDesc(String workspaceId);

    List<TestPlanEntity> findByWorkspaceIdAndProjectIdOrderByUpdatedAtDesc(String workspaceId, String projectId);
}
