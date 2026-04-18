package com.sdlctower.domain.codebuildmanagement.service;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiNoteCountsDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiPrReviewDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RegenerateAiPrReviewRequest;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RegenerateAiPrReviewResponse;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiRowStatus;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildAccessGuard;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildAiAutonomyPolicy;
import com.sdlctower.domain.codebuildmanagement.policy.HeadShaFencingPolicy;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("codeBuildManagementAiPrReviewCommandService")
public class AiPrReviewCommandService {

    private final CodeBuildAccessGuard accessGuard;
    private final HeadShaFencingPolicy headShaFencingPolicy;
    private final CodeBuildAiAutonomyPolicy autonomyPolicy;

    public AiPrReviewCommandService(
            CodeBuildAccessGuard accessGuard,
            HeadShaFencingPolicy headShaFencingPolicy,
            CodeBuildAiAutonomyPolicy autonomyPolicy
    ) {
        this.accessGuard = accessGuard;
        this.headShaFencingPolicy = headShaFencingPolicy;
        this.autonomyPolicy = autonomyPolicy;
    }

    public RegenerateAiPrReviewResponse regenerate(String prId, RegenerateAiPrReviewRequest request, String memberId) {
        accessGuard.requireAdmin(prId, memberId);
        headShaFencingPolicy.check(prId, request.prevHeadSha());
        autonomyPolicy.requireAtLeast("ws-default-001", "SUPERVISED");

        String skillVersion = "pr-review-v1.0.0";
        Instant generatedAt = Instant.now();

        AiPrReviewDto review = new AiPrReviewDto(
                AiRowStatus.SUCCESS,
                request.prevHeadSha(),
                skillVersion,
                generatedAt,
                new AiNoteCountsDto(0, 0, 0, 0),
                Map.of(),
                null
        );

        return new RegenerateAiPrReviewResponse(review, skillVersion, generatedAt);
    }
}
