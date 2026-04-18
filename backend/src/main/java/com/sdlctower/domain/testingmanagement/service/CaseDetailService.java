package com.sdlctower.domain.testingmanagement.service;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CaseDetailAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CaseDetailDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CaseRevisionDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CaseRunOutcomeDto;
import com.sdlctower.domain.testingmanagement.policy.TestingAccessGuard;
import com.sdlctower.domain.testingmanagement.projection.CaseDetailProjection;
import com.sdlctower.domain.testingmanagement.projection.CaseRecentResultsProjection;
import com.sdlctower.domain.testingmanagement.projection.PlanHeaderProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("testingManagementCaseDetailService")
public class CaseDetailService {

    private final CaseDetailProjection caseDetailProjection;
    private final CaseRecentResultsProjection caseRecentResultsProjection;
    private final PlanHeaderProjection planHeaderProjection;
    private final TestingAccessGuard accessGuard;

    public CaseDetailService(
            CaseDetailProjection caseDetailProjection,
            CaseRecentResultsProjection caseRecentResultsProjection,
            PlanHeaderProjection planHeaderProjection,
            TestingAccessGuard accessGuard
    ) {
        this.caseDetailProjection = caseDetailProjection;
        this.caseRecentResultsProjection = caseRecentResultsProjection;
        this.planHeaderProjection = planHeaderProjection;
        this.accessGuard = accessGuard;
    }

    public CaseDetailAggregateDto load(String caseId) {
        CaseDetailDto detail = caseDetailProjection.load(caseId);
        accessGuard.requireProjectRead(planHeaderProjection.load(detail.planId()).projectId());
        return new CaseDetailAggregateDto(
                SectionResultDto.ok(detail),
                SectionResultDto.ok(caseRecentResultsProjection.load(caseId)),
                SectionResultDto.ok(List.<CaseRevisionDto>of())
        );
    }

    public CaseDetailDto loadDetail(String caseId) {
        return load(caseId).detail().data();
    }

    public List<CaseRunOutcomeDto> loadRecentResults(String caseId) {
        return load(caseId).recentResults().data();
    }
}
