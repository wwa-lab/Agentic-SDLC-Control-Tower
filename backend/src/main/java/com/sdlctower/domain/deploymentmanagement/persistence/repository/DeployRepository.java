package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.DeployEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DeployRepository extends JpaRepository<DeployEntity, String> {

    List<DeployEntity> findTop20ByApplicationIdOrderByStartedAtDesc(String applicationId);

    Optional<DeployEntity> findByJenkinsInstanceIdAndJenkinsJobFullNameAndJenkinsBuildNumber(
            String jenkinsInstanceId, String jenkinsJobFullName, int jenkinsBuildNumber);

    @Query("SELECT d FROM DeployEntity d WHERE d.applicationId = :appId AND d.environmentName = :env " +
           "AND d.state = 'SUCCEEDED' ORDER BY d.completedAt DESC LIMIT 1")
    Optional<DeployEntity> findLastSuccessByApplicationAndEnvironment(
            @Param("appId") String applicationId, @Param("env") String environmentName);

    List<DeployEntity> findByReleaseIdOrderByStartedAtDesc(String releaseId);

    List<DeployEntity> findByApplicationIdAndEnvironmentNameOrderByStartedAtDesc(
            String applicationId, String environmentName);

    long countByApplicationIdAndState(String applicationId, String state);
}
