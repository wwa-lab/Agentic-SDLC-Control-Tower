package com.sdlctower.domain.deploymentmanagement.service;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.*;
import com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.*;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.*;
import com.sdlctower.shared.dto.SectionResultDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("deploymentApplicationDetailService")
public class ApplicationDetailService {

    private final ApplicationRepository appRepo;
    private final ApplicationEnvironmentRepository envRepo;
    private final ReleaseRepository releaseRepo;
    private final DeployRepository deployRepo;

    public ApplicationDetailService(ApplicationRepository appRepo, ApplicationEnvironmentRepository envRepo,
                                     ReleaseRepository releaseRepo, DeployRepository deployRepo) {
        this.appRepo = appRepo;
        this.envRepo = envRepo;
        this.releaseRepo = releaseRepo;
        this.deployRepo = deployRepo;
    }

    public ApplicationDetailAggregateDto loadAggregate(String applicationId) {
        var header = buildHeader(applicationId);
        var envs = buildEnvironments(applicationId);
        var releases = buildRecentReleases(applicationId);
        var deploys = buildRecentDeploys(applicationId);
        return new ApplicationDetailAggregateDto(header, envs, releases, deploys,
                SectionResultDto.ok(List.of()), SectionResultDto.ok(new ApplicationAiInsightDto(
                        AiRowStatus.PENDING, null, null, null, null, null)));
    }

    private SectionResultDto<ApplicationHeaderDto> buildHeader(String appId) {
        try {
            var app = appRepo.findById(appId).orElseThrow(() ->
                    new RuntimeException("Application not found: " + appId));
            return SectionResultDto.ok(new ApplicationHeaderDto(
                    app.getId(), app.getName(), app.getProjectId(), app.getWorkspaceId(),
                    app.getRuntimeLabel(), app.getJenkinsFolderPath(), "", null, app.getDescription()));
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }

    private SectionResultDto<List<EnvironmentRowDto>> buildEnvironments(String appId) {
        try {
            var envs = envRepo.findByApplicationId(appId);
            var rows = envs.stream().map(env -> new EnvironmentRowDto(
                    env.getName(), EnvironmentKind.valueOf(env.getKind()),
                    null, null, null, null, null, null, false, null)).toList();
            return SectionResultDto.ok(rows);
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }

    private SectionResultDto<List<RecentReleaseRowDto>> buildRecentReleases(String appId) {
        try {
            var releases = releaseRepo.findTop20ByApplicationIdOrderByCreatedAtDesc(appId);
            var rows = releases.stream().map(r -> new RecentReleaseRowDto(
                    r.getId(), r.getReleaseVersion(),
                    new BuildArtifactRefDto(r.getBuildArtifactSliceId(), r.getBuildArtifactId()),
                    r.getCreatedAt(), ReleaseState.valueOf(r.getState()), 0)).toList();
            return SectionResultDto.ok(rows);
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }

    private SectionResultDto<List<RecentDeployRowDto>> buildRecentDeploys(String appId) {
        try {
            var deploys = deployRepo.findTop20ByApplicationIdOrderByStartedAtDesc(appId);
            var rows = deploys.stream().map(d -> new RecentDeployRowDto(
                    d.getId(), d.getReleaseId(), d.getEnvironmentName(),
                    DeployState.valueOf(d.getState()), d.getStartedAt(), d.getDurationSec(),
                    null, false, d.isRollback())).toList();
            return SectionResultDto.ok(rows);
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }
}
