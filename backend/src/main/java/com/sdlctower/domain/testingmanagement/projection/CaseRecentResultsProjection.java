package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CaseRunOutcomeDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestResultOutcome;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestEnvironmentEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestEnvironmentRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestFailureSummaryEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestFailureSummaryRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestRunEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestRunRepository;
import com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component("testingManagementCaseRecentResultsProjection")
public class CaseRecentResultsProjection {

    private final TestCaseResultRepository testCaseResultRepository;
    private final TestFailureSummaryRepository testFailureSummaryRepository;
    private final TestRunRepository testRunRepository;
    private final TestEnvironmentRepository testEnvironmentRepository;
    private final TestingManagementReadSupport readSupport;

    public CaseRecentResultsProjection(
            TestCaseResultRepository testCaseResultRepository,
            TestFailureSummaryRepository testFailureSummaryRepository,
            TestRunRepository testRunRepository,
            TestEnvironmentRepository testEnvironmentRepository,
            TestingManagementReadSupport readSupport
    ) {
        this.testCaseResultRepository = testCaseResultRepository;
        this.testFailureSummaryRepository = testFailureSummaryRepository;
        this.testRunRepository = testRunRepository;
        this.testEnvironmentRepository = testEnvironmentRepository;
        this.readSupport = readSupport;
    }

    public List<CaseRunOutcomeDto> load(String caseId) {
        List<TestCaseResultEntity> results = testCaseResultRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream().limit(20).toList();
        Map<String, TestFailureSummaryEntity> summaries = testFailureSummaryRepository.findByResultIdIn(results.stream()
                        .map(TestCaseResultEntity::getId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(TestFailureSummaryEntity::getResultId, summary -> summary));
        Map<String, TestRunEntity> runs = testRunRepository.findAllById(results.stream().map(TestCaseResultEntity::getRunId).toList())
                .stream()
                .collect(Collectors.toMap(TestRunEntity::getId, run -> run));
        Map<String, TestEnvironmentEntity> environments = testEnvironmentRepository.findByIdIn(runs.values().stream()
                        .map(TestRunEntity::getEnvironmentId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(TestEnvironmentEntity::getId, environment -> environment));

        return results.stream()
                .map(result -> {
                    TestFailureSummaryEntity summary = summaries.get(result.getId());
                    TestRunEntity run = runs.get(result.getRunId());
                    TestEnvironmentEntity environment = run == null ? null : environments.get(run.getEnvironmentId());
                    return new CaseRunOutcomeDto(
                            result.getId(),
                            result.getRunId(),
                            TestResultOutcome.valueOf(result.getOutcome()),
                            summary == null ? null : readSupport.truncateFailureExcerpt(summary.getFailureExcerpt()),
                            result.getLastPassedAt(),
                            environment == null ? null : environment.getName(),
                            result.getCreatedAt()
                    );
                })
                .toList();
    }
}
