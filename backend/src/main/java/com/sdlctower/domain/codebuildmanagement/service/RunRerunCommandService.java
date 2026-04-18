package com.sdlctower.domain.codebuildmanagement.service;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RerunRunRequest;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RerunRunResponse;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildAccessGuard;
import com.sdlctower.domain.codebuildmanagement.policy.RunRerunRatePolicy;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service("codeBuildManagementRunRerunCommandService")
public class RunRerunCommandService {

    private final CodeBuildAccessGuard accessGuard;
    private final RunRerunRatePolicy rerunRatePolicy;

    public RunRerunCommandService(
            CodeBuildAccessGuard accessGuard,
            RunRerunRatePolicy rerunRatePolicy
    ) {
        this.accessGuard = accessGuard;
        this.rerunRatePolicy = rerunRatePolicy;
    }

    public RerunRunResponse rerun(String runId, RerunRunRequest request, String memberId) {
        accessGuard.requireAdmin(runId, memberId);

        String repoId = "repo-cb-001";
        rerunRatePolicy.check(repoId);
        rerunRatePolicy.record(repoId);

        String newRunId = "run-rerun-" + UUID.randomUUID().toString().substring(0, 8);
        return new RerunRunResponse(newRunId, RunStatus.QUEUED, Instant.now());
    }
}
