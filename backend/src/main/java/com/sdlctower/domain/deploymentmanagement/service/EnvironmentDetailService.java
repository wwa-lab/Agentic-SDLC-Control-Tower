package com.sdlctower.domain.deploymentmanagement.service;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.*;
import com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.*;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.*;
import com.sdlctower.shared.dto.SectionResultDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("deploymentEnvironmentDetailService")
public class EnvironmentDetailService {

    private final ApplicationEnvironmentRepository envRepo;
    private final DeployRepository deployRepo;

    public EnvironmentDetailService(ApplicationEnvironmentRepository envRepo, DeployRepository deployRepo) {
        this.envRepo = envRepo;
        this.deployRepo = deployRepo;
    }

    public EnvironmentDetailAggregateDto loadAggregate(String applicationId, String environmentName) {
        var header = buildHeader(applicationId, environmentName);
        var timeline = buildTimeline(applicationId, environmentName);
        return new EnvironmentDetailAggregateDto(header,
                SectionResultDto.ok(new EnvironmentRevisionsDto(null, null, null, null, null, null, null, null)),
                timeline,
                SectionResultDto.ok(new EnvironmentMetricsDto(0.0, null, 0, 0, 0.0)));
    }

    private SectionResultDto<EnvironmentHeaderDto> buildHeader(String appId, String envName) {
        try {
            var env = envRepo.findByApplicationIdAndName(appId, envName).orElseThrow(() ->
                    new RuntimeException("Environment not found: " + appId + ":" + envName));
            return SectionResultDto.ok(new EnvironmentHeaderDto(
                    appId, env.getName(), EnvironmentKind.valueOf(env.getKind())));
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }

    private SectionResultDto<List<EnvironmentTimelineEntryDto>> buildTimeline(String appId, String envName) {
        try {
            var deploys = deployRepo.findByApplicationIdAndEnvironmentNameOrderByStartedAtDesc(appId, envName);
            var rows = deploys.stream().map(d -> new EnvironmentTimelineEntryDto(
                    d.getId(), d.getReleaseId(), DeployState.valueOf(d.getState()),
                    d.getStartedAt(), d.getDurationSec(), d.isRollback())).toList();
            return SectionResultDto.ok(rows);
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }
}
