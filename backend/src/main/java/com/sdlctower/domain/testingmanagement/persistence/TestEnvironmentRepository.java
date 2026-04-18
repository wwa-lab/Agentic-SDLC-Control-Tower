package com.sdlctower.domain.testingmanagement.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestEnvironmentRepository extends JpaRepository<TestEnvironmentEntity, String> {

    List<TestEnvironmentEntity> findByIdIn(Collection<String> ids);
}
