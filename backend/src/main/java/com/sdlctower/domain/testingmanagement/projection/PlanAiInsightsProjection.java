package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.TestingManagementConstants;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.AiInsightsDto;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestRunEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestRunRepository;
import com.sdlctower.shared.integration.workspace.WorkspaceAutonomyLookup;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("testingManagementPlanAiInsightsProjection")
public class PlanAiInsightsProjection {

    private final TestCaseRepository testCaseRepository;
    private final TestRunRepository testRunRepository;
    private final PlanHeaderProjection planHeaderProjection;
    private final WorkspaceAutonomyLookup workspaceAutonomyLookup;

    public PlanAiInsightsProjection(
            TestCaseRepository testCaseRepository,
            TestRunRepository testRunRepository,
            PlanHeaderProjection planHeaderProjection,
            WorkspaceAutonomyLookup workspaceAutonomyLookup
    ) {
        this.testCaseRepository = testCaseRepository;
        this.testRunRepository = testRunRepository;
        this.planHeaderProjection = planHeaderProjection;
        this.workspaceAutonomyLookup = workspaceAutonomyLookup;
    }

    public AiInsightsDto load(String planId) {
        String workspaceId = planHeaderProjection.load(planId).workspaceId();
        List<TestRunEntity> runs = testRunRepository.findByPlanIdOrderByStartedAtDesc(planId).stream()
                .filter(run -> run.getStartedAt().isAfter(TestingManagementConstants.REFERENCE_NOW.minus(TestingManagementConstants.RECENT_WINDOW)))
                .toList();
        int pass = runs.stream().mapToInt(TestRunEntity::getPassCount).sum();
        int fail = runs.stream().mapToInt(run -> run.getFailCount() + run.getErrorCount()).sum();
        double passRate = pass + fail == 0 ? 0d : (double) pass / (double) (pass + fail);
        int pendingDrafts = (int) testCaseRepository.findByPlanIdAndStateOrderByCreatedAtAsc(planId, "DRAFT").stream()
                .filter(testCase -> "AI_DRAFT".equals(testCase.getOrigin()))
                .count();
        int activeCases = (int) testCaseRepository.findByPlanIdAndStateOrderByCreatedAtAsc(planId, "ACTIVE").size();
        String autonomy = workspaceAutonomyLookup.currentLevel(workspaceId).name();
        return new AiInsightsDto(
                autonomy,
                pendingDrafts,
                activeCases,
                passRate,
                pendingDrafts > 0
                        ? pendingDrafts + " AI draft cases are waiting for review while recent pass rate sits at " + Math.round(passRate * 100d) + "%."
                        : "No pending AI drafts. Recent plan pass rate is " + Math.round(passRate * 100d) + "%."
        );
    }
}
