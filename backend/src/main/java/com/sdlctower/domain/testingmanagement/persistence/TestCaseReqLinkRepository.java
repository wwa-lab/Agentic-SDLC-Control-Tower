package com.sdlctower.domain.testingmanagement.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseReqLinkRepository extends JpaRepository<TestCaseReqLinkEntity, Long> {

    List<TestCaseReqLinkEntity> findByCaseId(String caseId);

    List<TestCaseReqLinkEntity> findByCaseIdIn(Collection<String> caseIds);
}
