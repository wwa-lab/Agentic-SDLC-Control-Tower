package com.sdlctower.domain.codebuildmanagement.service;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiTriageDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RegenerateAiTriageRequest;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RegenerateAiTriageResponse;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiRowStatus;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildAccessGuard;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildAiAutonomyPolicy;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("codeBuildManagementAiTriageCommandService")
public class AiTriageCommandService {

    private final CodeBuildAccessGuard accessGuard;
    private final CodeBuildAiAutonomyPolicy autonomyPolicy;

    public AiTriageCommandService(
            CodeBuildAccessGuard accessGuard,
            CodeBuildAiAutonomyPolicy autonomyPolicy
    ) {
        this.accessGuard = accessGuard;
        this.autonomyPolicy = autonomyPolicy;
    }

    public RegenerateAiTriageResponse regenerate(String runId, RegenerateAiTriageRequest request, String memberId) {
        accessGuard.requireAdmin(runId, memberId);
        autonomyPolicy.requireAtLeast("ws-default-001", "SUPERVISED");

        String skillVersion = "triage-v1.0.0";
        Instant generatedAt = Instant.now();

        AiTriageDto triage = new AiTriageDto(
                AiRowStatus.SUCCESS,
                skillVersion,
                generatedAt,
                List.of(),
                null
        );

        return new RegenerateAiTriageResponse(triage, skillVersion, generatedAt);
    }
}
