package com.sdlctower.domain.testingmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRunRepository extends JpaRepository<TestRunEntity, String> {

    List<TestRunEntity> findByPlanIdOrderByStartedAtDesc(String planId);
}
