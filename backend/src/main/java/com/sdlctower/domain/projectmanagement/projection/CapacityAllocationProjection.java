package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioCapacityCellDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioCapacityDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioCapacityProjectDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioCapacityRowDto;
import com.sdlctower.domain.projectmanagement.persistence.CapacityAllocationRepository;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CapacityAllocationProjection {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final CapacityAllocationRepository capacityAllocationRepository;

    public CapacityAllocationProjection(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            CapacityAllocationRepository capacityAllocationRepository
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.capacityAllocationRepository = capacityAllocationRepository;
    }

    public PortfolioCapacityDto load(String workspaceId) {
        List<PortfolioCapacityProjectDto> projects = projectSeedCatalog.projectsForWorkspace(workspaceId).stream()
                .map(project -> new PortfolioCapacityProjectDto(project.id(), project.name()))
                .toList();

        Map<String, Map<String, Integer>> memberProjectPercents = new LinkedHashMap<>();
        for (var project : projectSeedCatalog.projectsForWorkspace(workspaceId)) {
            capacityAllocationRepository.findByProjectIdOrderByWindowStartAscMemberIdAsc(project.id()).forEach(cell -> {
                memberProjectPercents
                        .computeIfAbsent(cell.getMemberId(), ignored -> new LinkedHashMap<>())
                        .merge(project.id(), cell.getAllocationPercent(), Integer::sum);
            });
        }

        List<PortfolioCapacityRowDto> rows = new ArrayList<>();
        memberProjectPercents.forEach((memberId, allocations) -> {
            int total = allocations.values().stream().mapToInt(Integer::intValue).sum();
            String flag = total > 100 ? "OVER" : total < ProjectManagementConstants.CAPACITY_WARNING_THRESHOLD ? "UNDER" : "BALANCED";
            List<PortfolioCapacityCellDto> cells = allocations.entrySet().stream()
                    .map(entry -> new PortfolioCapacityCellDto(entry.getKey(), entry.getValue()))
                    .toList();
            rows.add(new PortfolioCapacityRowDto(
                    memberId,
                    projectSeedCatalog.memberDisplayName(memberId),
                    total,
                    flag,
                    cells
            ));
        });

        return new PortfolioCapacityDto(projects, rows, ProjectManagementConstants.CAPACITY_WARNING_THRESHOLD);
    }
}
