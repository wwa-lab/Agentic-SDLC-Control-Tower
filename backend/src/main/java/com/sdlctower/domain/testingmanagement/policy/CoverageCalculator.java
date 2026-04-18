package com.sdlctower.domain.testingmanagement.policy;

import com.sdlctower.domain.testingmanagement.TestingManagementConstants;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CoverageRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestResultOutcome;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultEntity;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("testingManagementCoverageCalculator")
public class CoverageCalculator {

    public CoverageStatus forLatestResults(List<TestCaseResultEntity> latestResults) {
        if (latestResults == null || latestResults.isEmpty()) {
            return CoverageStatus.GREY;
        }

        Instant latestAt = latestResults.stream()
                .map(TestCaseResultEntity::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(TestingManagementConstants.REFERENCE_NOW);
        if (latestAt.isBefore(TestingManagementConstants.REFERENCE_NOW.minus(TestingManagementConstants.RECENT_WINDOW))) {
            return CoverageStatus.AMBER;
        }

        EnumSet<TestResultOutcome> outcomes = latestResults.stream()
                .map(result -> TestResultOutcome.valueOf(result.getOutcome()))
                .collect(() -> EnumSet.noneOf(TestResultOutcome.class), EnumSet::add, EnumSet::addAll);

        boolean hasPass = outcomes.contains(TestResultOutcome.PASS);
        boolean hasFailure = outcomes.contains(TestResultOutcome.FAIL) || outcomes.contains(TestResultOutcome.ERROR);

        if (hasPass && hasFailure) {
            return CoverageStatus.AMBER;
        }
        if (hasFailure) {
            return CoverageStatus.RED;
        }
        if (hasPass) {
            return CoverageStatus.GREEN;
        }
        return CoverageStatus.AMBER;
    }

    public CoverageStatus forPlanLed(List<CoverageRowDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return CoverageStatus.GREY;
        }
        long green = rows.stream().filter(row -> row.aggregateStatus() == CoverageStatus.GREEN).count();
        double ratio = (double) green / (double) rows.size();
        if (ratio >= 0.8d) {
            return CoverageStatus.GREEN;
        }
        if (ratio >= 0.5d) {
            return CoverageStatus.AMBER;
        }
        return CoverageStatus.RED;
    }
}
