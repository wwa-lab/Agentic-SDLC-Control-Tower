package com.sdlctower.domain.deploymentmanagement.service;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.*;
import com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.*;
import com.sdlctower.domain.deploymentmanagement.persistence.entity.*;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.*;
import com.sdlctower.shared.dto.SectionResultDto;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service("deploymentCatalogService")
public class CatalogService {

    private final ApplicationRepository applicationRepo;
    private final ApplicationEnvironmentRepository envRepo;
    private final DeployRepository deployRepo;

    public CatalogService(ApplicationRepository applicationRepo,
                          ApplicationEnvironmentRepository envRepo,
                          DeployRepository deployRepo) {
        this.applicationRepo = applicationRepo;
        this.envRepo = envRepo;
        this.deployRepo = deployRepo;
    }

    public CatalogAggregateDto loadAggregate(CatalogFiltersDto filters) {
        var summary = buildSummary();
        var grid = buildGrid();
        return new CatalogAggregateDto(summary, grid,
                SectionResultDto.ok(new AiWorkspaceDeploymentSummaryDto(AiRowStatus.PENDING, null, null, null)),
                filters);
    }

    private SectionResultDto<CatalogSummaryDto> buildSummary() {
        try {
            long total = applicationRepo.count();
            return SectionResultDto.ok(new CatalogSummaryDto(
                    total, 0, 0, 0.0, 0.0, 0.0, Map.of(HealthLed.GREEN, total)));
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }

    private SectionResultDto<List<CatalogSectionDto>> buildGrid() {
        try {
            var apps = applicationRepo.findAll();
            var grouped = apps.stream().collect(Collectors.groupingBy(ApplicationEntity::getProjectId));
            var sections = grouped.entrySet().stream().map(entry -> {
                var tiles = entry.getValue().stream().map(app -> {
                    var envs = envRepo.findByApplicationId(app.getId());
                    var chips = envs.stream().map(env -> {
                        var deploys = deployRepo.findByApplicationIdAndEnvironmentNameOrderByStartedAtDesc(
                                app.getId(), env.getName());
                        var latest = deploys.isEmpty() ? null : deploys.getFirst();
                        return new EnvRevisionChipDto(
                                env.getName(),
                                latest != null && latest.getReleaseId() != null ? latest.getReleaseId() : null,
                                latest != null ? DeployState.valueOf(latest.getState()) : null,
                                latest != null ? latest.getCompletedAt() : null,
                                latest != null && latest.isRollback());
                    }).toList();
                    return new CatalogApplicationTileDto(
                            app.getId(), app.getName(), app.getProjectId(), app.getWorkspaceId(),
                            app.getRuntimeLabel(), null, chips, HealthLed.GREEN, app.getDescription());
                }).toList();
                return new CatalogSectionDto(entry.getKey(), entry.getKey(), tiles);
            }).toList();
            return SectionResultDto.ok(sections);
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }
}
