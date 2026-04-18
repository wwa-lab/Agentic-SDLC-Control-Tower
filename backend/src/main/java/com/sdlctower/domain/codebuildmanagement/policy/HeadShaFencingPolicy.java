package com.sdlctower.domain.codebuildmanagement.policy;

import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestRepository;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementHeadShaFencingPolicy")
public class HeadShaFencingPolicy {

    private final PullRequestRepository pullRequestRepository;

    public HeadShaFencingPolicy(PullRequestRepository pullRequestRepository) {
        this.pullRequestRepository = pullRequestRepository;
    }

    public void check(String prId, String clientPrevHeadSha) {
        PullRequestEntity pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_PR_NOT_FOUND", "Pull request not found: " + prId));

        String currentHeadSha = pr.getHeadSha();

        if (clientPrevHeadSha != null && !clientPrevHeadSha.equals(currentHeadSha)) {
            throw CodeBuildManagementException.conflict(
                    "CB_STALE_HEAD_SHA",
                    "Head SHA has advanced from " + clientPrevHeadSha
                            + " to " + currentHeadSha
                            + " on PR " + prId);
        }
    }
}
