package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogFiltersDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestPlanState;
import com.sdlctower.domain.testingmanagement.persistence.TestPlanEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestPlanRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("testingManagementCatalogFiltersProjection")
public class CatalogFiltersProjection {

    private final TestPlanRepository testPlanRepository;

    public CatalogFiltersProjection(TestPlanRepository testPlanRepository) {
        this.testPlanRepository = testPlanRepository;
    }

    public CatalogFiltersDto load(String workspaceId) {
        List<TestPlanEntity> plans = testPlanRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId);
        return new CatalogFiltersDto(
                plans.stream().map(TestPlanEntity::getProjectId).distinct().sorted().toList(),
                plans.stream()
                        .map(plan -> TestPlanState.valueOf(plan.getState()))
                        .distinct()
                        .sorted(Comparator.comparing(Enum::name))
                        .toList(),
                List.of(CoverageStatus.GREEN, CoverageStatus.AMBER, CoverageStatus.RED, CoverageStatus.GREY)
        );
    }
}
