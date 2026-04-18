package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.PlanHeaderDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestPlanState;
import com.sdlctower.domain.testingmanagement.persistence.TestPlanEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestPlanRepository;
import com.sdlctower.domain.testingmanagement.policy.TestingManagementException;
import com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport;
import org.springframework.stereotype.Component;

@Component("testingManagementPlanHeaderProjection")
public class PlanHeaderProjection {

    private final TestPlanRepository testPlanRepository;
    private final TestingManagementReadSupport readSupport;

    public PlanHeaderProjection(
            TestPlanRepository testPlanRepository,
            TestingManagementReadSupport readSupport
    ) {
        this.testPlanRepository = testPlanRepository;
        this.readSupport = readSupport;
    }

    public PlanHeaderDto load(String planId) {
        TestPlanEntity plan = testPlanRepository.findById(planId)
                .orElseThrow(() -> TestingManagementException.notFound("TM_PLAN_NOT_FOUND", "Plan not found: " + planId));
        return new PlanHeaderDto(
                plan.getId(),
                plan.getProjectId(),
                readSupport.projectName(plan.getProjectId()),
                plan.getWorkspaceId(),
                readSupport.workspaceName(plan.getWorkspaceId()),
                plan.getName(),
                plan.getDescription(),
                plan.getReleaseTarget(),
                readSupport.memberRef(plan.getOwnerMemberId()),
                TestPlanState.valueOf(plan.getState()),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
