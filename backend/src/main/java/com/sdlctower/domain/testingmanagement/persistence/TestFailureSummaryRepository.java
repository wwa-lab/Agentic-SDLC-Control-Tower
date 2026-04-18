package com.sdlctower.domain.testingmanagement.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestFailureSummaryRepository extends JpaRepository<TestFailureSummaryEntity, Long> {

    List<TestFailureSummaryEntity> findByResultIdIn(Collection<String> resultIds);
}
