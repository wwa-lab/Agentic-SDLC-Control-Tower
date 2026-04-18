package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunCaseResultRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestResultOutcome;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestFailureSummaryEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestFailureSummaryRepository;
import com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component("testingManagementRunCaseResultsProjection")
public class RunCaseResultsProjection {

    private final TestCaseResultRepository testCaseResultRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestFailureSummaryRepository testFailureSummaryRepository;
    private final TestingManagementReadSupport readSupport;

    public RunCaseResultsProjection(
            TestCaseResultRepository testCaseResultRepository,
            TestCaseRepository testCaseRepository,
            TestFailureSummaryRepository testFailureSummaryRepository,
            TestingManagementReadSupport readSupport
    ) {
        this.testCaseResultRepository = testCaseResultRepository;
        this.testCaseRepository = testCaseRepository;
        this.testFailureSummaryRepository = testFailureSummaryRepository;
        this.readSupport = readSupport;
    }

    public List<RunCaseResultRowDto> load(String runId) {
        List<TestCaseResultEntity> results = testCaseResultRepository.findByRunIdOrderByCreatedAtAsc(runId);
        Map<String, TestCaseEntity> cases = testCaseRepository.findByIdIn(results.stream().map(TestCaseResultEntity::getCaseId).toList())
                .stream()
                .collect(Collectors.toMap(TestCaseEntity::getId, testCase -> testCase));
        Map<String, TestFailureSummaryEntity> summaries = testFailureSummaryRepository.findByResultIdIn(results.stream()
                        .map(TestCaseResultEntity::getId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(TestFailureSummaryEntity::getResultId, summary -> summary));

        return results.stream()
                .map(result -> new RunCaseResultRowDto(
                        result.getId(),
                        result.getCaseId(),
                        cases.get(result.getCaseId()) == null ? result.getCaseId() : cases.get(result.getCaseId()).getTitle(),
                        TestResultOutcome.valueOf(result.getOutcome()),
                        result.getDurationSec(),
                        summaries.get(result.getId()) == null ? null : readSupport.truncateFailureExcerpt(summaries.get(result.getId()).getFailureExcerpt()),
                        result.getLastPassedAt(),
                        result.getCreatedAt()
                ))
                .toList();
    }
}
