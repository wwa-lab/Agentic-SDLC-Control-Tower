package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogPlanRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestPlanState;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestPlanEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestPlanRepository;
import com.sdlctower.domain.testingmanagement.policy.CoverageCalculator;
import com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component("testingManagementCatalogGridProjection")
public class CatalogGridProjection {

    private final TestPlanRepository testPlanRepository;
    private final TestCaseRepository testCaseRepository;
    private final PlanCoverageProjection planCoverageProjection;
    private final CoverageCalculator coverageCalculator;
    private final TestingManagementReadSupport readSupport;

    public CatalogGridProjection(
            TestPlanRepository testPlanRepository,
            TestCaseRepository testCaseRepository,
            PlanCoverageProjection planCoverageProjection,
            CoverageCalculator coverageCalculator,
            TestingManagementReadSupport readSupport
    ) {
        this.testPlanRepository = testPlanRepository;
        this.testCaseRepository = testCaseRepository;
        this.planCoverageProjection = planCoverageProjection;
        this.coverageCalculator = coverageCalculator;
        this.readSupport = readSupport;
    }

    public List<CatalogPlanRowDto> load(
            String workspaceId,
            String projectId,
            String planState,
            String coverageLed,
            String search
    ) {
        List<TestPlanEntity> plans = projectId == null || projectId.isBlank()
                ? testPlanRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId)
                : testPlanRepository.findByWorkspaceIdAndProjectIdOrderByUpdatedAtDesc(workspaceId, projectId);

        return plans.stream()
                .filter(plan -> matchesPlanState(plan, planState))
                .map(plan -> {
                    List<com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CoverageRowDto> coverageRows = planCoverageProjection.load(plan.getId());
                    CoverageStatus led = coverageCalculator.forPlanLed(coverageRows);
                    return new CatalogPlanRowDto(
                            plan.getId(),
                            plan.getProjectId(),
                            readSupport.projectName(plan.getProjectId()),
                            plan.getWorkspaceId(),
                            plan.getName(),
                            plan.getReleaseTarget(),
                            readSupport.memberRef(plan.getOwnerMemberId()),
                            TestPlanState.valueOf(plan.getState()),
                            testCaseRepository.findByPlanIdOrderByCreatedAtAsc(plan.getId()).size(),
                            led,
                            plan.getDescription(),
                            plan.getCreatedAt(),
                            plan.getUpdatedAt()
                    );
                })
                .filter(row -> matchesCoverage(row, coverageLed))
                .filter(row -> matchesSearch(row, search))
                .toList();
    }

    private boolean matchesPlanState(TestPlanEntity plan, String planState) {
        return planState == null || planState.isBlank() || plan.getState().equalsIgnoreCase(planState);
    }

    private boolean matchesCoverage(CatalogPlanRowDto row, String coverageLed) {
        return coverageLed == null || coverageLed.isBlank() || row.coverageLed().name().equalsIgnoreCase(coverageLed);
    }

    private boolean matchesSearch(CatalogPlanRowDto row, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return Stream.of(row.name(), row.projectName(), row.description(), row.owner().displayName())
                .filter(value -> value != null)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(needle));
    }
}
