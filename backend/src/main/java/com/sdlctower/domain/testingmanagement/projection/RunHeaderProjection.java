package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunHeaderDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.RunTriggerSource;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestEnvironmentKind;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestRunState;
import com.sdlctower.domain.testingmanagement.persistence.TestEnvironmentEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestEnvironmentRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestRunEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestRunRepository;
import com.sdlctower.domain.testingmanagement.policy.TestingManagementException;
import com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport;
import org.springframework.stereotype.Component;

@Component("testingManagementRunHeaderProjection")
public class RunHeaderProjection {

    private final TestRunRepository testRunRepository;
    private final TestEnvironmentRepository testEnvironmentRepository;
    private final PlanHeaderProjection planHeaderProjection;
    private final TestingManagementReadSupport readSupport;

    public RunHeaderProjection(
            TestRunRepository testRunRepository,
            TestEnvironmentRepository testEnvironmentRepository,
            PlanHeaderProjection planHeaderProjection,
            TestingManagementReadSupport readSupport
    ) {
        this.testRunRepository = testRunRepository;
        this.testEnvironmentRepository = testEnvironmentRepository;
        this.planHeaderProjection = planHeaderProjection;
        this.readSupport = readSupport;
    }

    public RunHeaderDto load(String runId) {
        TestRunEntity run = testRunRepository.findById(runId)
                .orElseThrow(() -> TestingManagementException.notFound("TM_RUN_NOT_FOUND", "Run not found: " + runId));
        TestEnvironmentEntity environment = testEnvironmentRepository.findById(run.getEnvironmentId())
                .orElseThrow(() -> TestingManagementException.notFound("TM_ENV_NOT_FOUND", "Environment not found: " + run.getEnvironmentId()));
        return new RunHeaderDto(
                run.getId(),
                run.getPlanId(),
                planHeaderProjection.load(run.getPlanId()).projectId(),
                planHeaderProjection.load(run.getPlanId()).name(),
                environment.getId(),
                environment.getName(),
                TestEnvironmentKind.valueOf(environment.getKind()),
                RunTriggerSource.valueOf(run.getTriggerSource()),
                readSupport.memberRef(run.getActorMemberId()),
                TestRunState.valueOf(run.getState()),
                run.getExternalRunId(),
                run.getDurationSec(),
                run.getStartedAt(),
                run.getCompletedAt()
        );
    }
}
