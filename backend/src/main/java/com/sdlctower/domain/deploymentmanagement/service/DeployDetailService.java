package com.sdlctower.domain.deploymentmanagement.service;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.*;
import com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.*;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.*;
import com.sdlctower.shared.dto.SectionResultDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("deploymentDeployDetailService")
public class DeployDetailService {

    private final DeployRepository deployRepo;
    private final DeployStageRepository stageRepo;
    private final ApprovalEventRepository approvalRepo;

    public DeployDetailService(DeployRepository deployRepo, DeployStageRepository stageRepo,
                                ApprovalEventRepository approvalRepo) {
        this.deployRepo = deployRepo;
        this.stageRepo = stageRepo;
        this.approvalRepo = approvalRepo;
    }

    public DeployDetailAggregateDto loadAggregate(String deployId) {
        var header = buildHeader(deployId);
        var stages = buildStages(deployId);
        var approvals = buildApprovals(deployId);
        var d = deployRepo.findById(deployId).orElse(null);
        var ctx = new OpenIncidentContextDto(
                d != null ? d.getApplicationId() : "", d != null ? d.getEnvironmentName() : "",
                deployId, "", "/deployment/deploys/" + deployId, null);
        return new DeployDetailAggregateDto(header, stages, approvals,
                SectionResultDto.ok(null), ctx, null);
    }

    private SectionResultDto<DeployHeaderDto> buildHeader(String deployId) {
        try {
            var d = deployRepo.findById(deployId).orElseThrow(() ->
                    new RuntimeException("Deploy not found: " + deployId));
            return SectionResultDto.ok(new DeployHeaderDto(
                    d.getId(), d.getReleaseId(), "",
                    d.getApplicationId(), d.getEnvironmentName(),
                    d.getJenkinsJobFullName(), d.getJenkinsBuildNumber() != null ? d.getJenkinsBuildNumber() : 0,
                    d.getJenkinsBuildUrl(),
                    DeployTrigger.valueOf(d.getTrigger()), d.getActor(),
                    d.getStartedAt(), d.getCompletedAt(), d.getDurationSec(),
                    DeployState.valueOf(d.getState()), false, d.isRollback(), d.getReleaseId() == null));
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }

    private SectionResultDto<List<DeployStageRowDto>> buildStages(String deployId) {
        try {
            var stages = stageRepo.findByDeployIdOrderByStageOrderAsc(deployId);
            var rows = stages.stream().map(s -> new DeployStageRowDto(
                    s.getId(), s.getName(), s.getStageOrder(),
                    DeployStageState.valueOf(s.getState()),
                    s.getStartedAt(), s.getCompletedAt(), s.getDurationSec(),
                    null, null)).toList();
            return SectionResultDto.ok(rows);
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }

    private SectionResultDto<List<ApprovalEventDto>> buildApprovals(String deployId) {
        try {
            var events = approvalRepo.findByDeployIdOrderByDecidedAtAsc(deployId);
            var rows = events.stream().map(a -> new ApprovalEventDto(
                    a.getId(), a.getStageId(), a.getStageName(),
                    a.getApproverDisplayName(), a.getApproverMemberId(), a.getApproverRole(),
                    ApprovalDecision.valueOf(a.getDecision()), a.getGatePolicyVersion(),
                    a.getRationaleCipher(), a.getDecidedAt())).toList();
            return SectionResultDto.ok(rows);
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }
}
