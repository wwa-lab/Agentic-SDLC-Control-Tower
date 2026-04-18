package com.sdlctower.domain.testingmanagement.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseRepository extends JpaRepository<TestCaseEntity, String> {

    List<TestCaseEntity> findByPlanIdOrderByCreatedAtAsc(String planId);

    List<TestCaseEntity> findByPlanIdAndStateOrderByCreatedAtAsc(String planId, String state);

    List<TestCaseEntity> findByIdIn(Collection<String> ids);
}
