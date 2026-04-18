package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.TestingManagementConstants;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogPlanRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogSummaryDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestRunEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestRunRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component("testingManagementCatalogSummaryProjection")
public class CatalogSummaryProjection {

    private final CatalogGridProjection catalogGridProjection;
    private final TestCaseRepository testCaseRepository;
    private final TestRunRepository testRunRepository;

    public CatalogSummaryProjection(
            CatalogGridProjection catalogGridProjection,
            TestCaseRepository testCaseRepository,
            TestRunRepository testRunRepository
    ) {
        this.catalogGridProjection = catalogGridProjection;
        this.testCaseRepository = testCaseRepository;
        this.testRunRepository = testRunRepository;
    }

    public CatalogSummaryDto load(
            String workspaceId,
            String projectId,
            String planState,
            String coverageLed,
            String search
    ) {
        List<CatalogPlanRowDto> rows = catalogGridProjection.load(workspaceId, projectId, planState, coverageLed, search);
        List<TestRunEntity> recentRuns = rows.stream()
                .flatMap(row -> testRunRepository.findByPlanIdOrderByStartedAtDesc(row.planId()).stream())
                .filter(run -> run.getStartedAt().isAfter(TestingManagementConstants.REFERENCE_NOW.minus(TestingManagementConstants.RECENT_WINDOW)))
                .toList();

        double meanDuration = recentRuns.stream()
                .filter(run -> run.getDurationSec() != null)
                .mapToInt(TestRunEntity::getDurationSec)
                .average()
                .orElse(0d);
        int totalPass = recentRuns.stream().mapToInt(TestRunEntity::getPassCount).sum();
        int totalFail = recentRuns.stream().mapToInt(run -> run.getFailCount() + run.getErrorCount()).sum();
        double passRate = totalPass + totalFail == 0 ? 0d : (double) totalPass / (double) (totalPass + totalFail);
        Map<CoverageStatus, Long> byLed = rows.stream()
                .collect(Collectors.groupingBy(CatalogPlanRowDto::coverageLed, Collectors.counting()));

        return new CatalogSummaryDto(
                workspaceId,
                rows.size(),
                rows.stream().mapToInt(row -> testCaseRepository.findByPlanIdAndStateOrderByCreatedAtAsc(row.planId(), "ACTIVE").size()).sum(),
                recentRuns.size(),
                passRate,
                meanDuration,
                byLed
        );
    }
}
