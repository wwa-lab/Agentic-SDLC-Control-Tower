package com.sdlctower.domain.reportcenter.service;

import com.sdlctower.domain.reportcenter.config.ReportCenterProperties;
import com.sdlctower.domain.reportcenter.definitions.ReportDefinition;
import com.sdlctower.domain.reportcenter.definitions.ReportDefinitionRegistry;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.CatalogCategoryGroupDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.CatalogDto;
import com.sdlctower.domain.reportcenter.policy.ScopeAuthGuard;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportCatalogService {

    private final ReportDefinitionRegistry registry;
    private final ScopeAuthGuard scopeAuthGuard;
    private final ReportCenterProperties properties;

    public ReportCatalogService(ReportDefinitionRegistry registry, ScopeAuthGuard scopeAuthGuard, ReportCenterProperties properties) {
        this.registry = registry;
        this.scopeAuthGuard = scopeAuthGuard;
        this.properties = properties;
    }

    public CatalogDto getCatalogForCaller() {
        ScopeAuthGuard.AccessMatrix accessMatrix = scopeAuthGuard.listAccessibleScopes(properties.orgId());
        boolean hasAnyScope = !accessMatrix.orgIds().isEmpty() || !accessMatrix.workspaceIds().isEmpty() || !accessMatrix.projectIds().isEmpty();
        List<ReportDefinition> enabled = hasAnyScope ? registry.all() : List.of();
        return new CatalogDto(List.of(
                new CatalogCategoryGroupDto("efficiency", "Efficiency", enabled.stream().map(ReportDefinition::toDto).toList()),
                new CatalogCategoryGroupDto("quality", "Quality", List.of()),
                new CatalogCategoryGroupDto("stability", "Stability", List.of()),
                new CatalogCategoryGroupDto("governance", "Governance", List.of()),
                new CatalogCategoryGroupDto("ai-contribution", "AI Contribution", List.of())
        ));
    }
}
