package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RecentRunRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.RunTriggerSource;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestRunState;
import com.sdlctower.domain.testingmanagement.persistence.TestEnvironmentEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestEnvironmentRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestRunEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestRunRepository;
import com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component("testingManagementPlanRecentRunsProjection")
public class PlanRecentRunsProjection {

    private final TestRunRepository testRunRepository;
    private final TestEnvironmentRepository testEnvironmentRepository;
    private final TestingManagementReadSupport readSupport;

    public PlanRecentRunsProjection(
            TestRunRepository testRunRepository,
            TestEnvironmentRepository testEnvironmentRepository,
            TestingManagementReadSupport readSupport
    ) {
        this.testRunRepository = testRunRepository;
        this.testEnvironmentRepository = testEnvironmentRepository;
        this.readSupport = readSupport;
    }

    public List<RecentRunRowDto> load(String planId) {
        List<TestRunEntity> runs = testRunRepository.findByPlanIdOrderByStartedAtDesc(planId).stream().limit(20).toList();
        Map<String, TestEnvironmentEntity> environments = testEnvironmentRepository.findByIdIn(runs.stream()
                        .map(TestRunEntity::getEnvironmentId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(TestEnvironmentEntity::getId, entity -> entity));
        return runs.stream()
                .map(run -> {
                    TestEnvironmentEntity environment = environments.get(run.getEnvironmentId());
                    return new RecentRunRowDto(
                            run.getId(),
                            run.getEnvironmentId(),
                            environment == null ? run.getEnvironmentId() : environment.getName(),
                            RunTriggerSource.valueOf(run.getTriggerSource()),
                            TestRunState.valueOf(run.getState()),
                            run.getDurationSec(),
                            run.getPassCount(),
                            run.getFailCount() + run.getErrorCount(),
                            run.getSkipCount(),
                            readSupport.memberRef(run.getActorMemberId()),
                            run.getStartedAt(),
                            run.getCompletedAt()
                    );
                })
                .toList();
    }
}
