package com.sdlctower.domain.deploymentmanagement.policy;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.ApprovalEventDto;
import org.springframework.stereotype.Component;

@Component
public class ApprovalRedactionPolicy {

    private static final String REDACTED = "(redacted)";

    public ApprovalEventDto redact(ApprovalEventDto event, boolean hasViewApprovalsPermission) {
        if (hasViewApprovalsPermission) {
            return event;
        }
        return new ApprovalEventDto(
                event.approvalId(), event.stageId(), event.stageName(),
                REDACTED, event.approverMemberId(), event.approverRole(),
                event.decision(), event.gatePolicyVersion(),
                REDACTED, event.decidedAt());
    }
}
