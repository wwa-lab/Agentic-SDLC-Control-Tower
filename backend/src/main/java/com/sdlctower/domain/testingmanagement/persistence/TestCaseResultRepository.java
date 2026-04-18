package com.sdlctower.domain.testingmanagement.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseResultRepository extends JpaRepository<TestCaseResultEntity, String> {

    List<TestCaseResultEntity> findByRunIdOrderByCreatedAtAsc(String runId);

    List<TestCaseResultEntity> findByCaseIdInOrderByCreatedAtDesc(Collection<String> caseIds);

    List<TestCaseResultEntity> findByCaseIdOrderByCreatedAtDesc(String caseId);
}
