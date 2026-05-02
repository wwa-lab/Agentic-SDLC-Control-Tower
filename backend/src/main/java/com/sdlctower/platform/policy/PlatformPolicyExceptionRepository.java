package com.sdlctower.platform.policy;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformPolicyExceptionRepository extends JpaRepository<PlatformPolicyExceptionEntity, String> {
    List<PlatformPolicyExceptionEntity> findByPolicyIdOrderByCreatedAtDesc(String policyId);
    Optional<PlatformPolicyExceptionEntity> findByIdAndPolicyId(String id, String policyId);
}
