package com.sdlctower.domain.aicenter;

import com.sdlctower.domain.aicenter.dto.MetricsSummaryDto;
import com.sdlctower.domain.aicenter.dto.PageDto;
import com.sdlctower.domain.aicenter.dto.RunDetailDto;
import com.sdlctower.domain.aicenter.dto.RunDto;
import com.sdlctower.domain.aicenter.dto.RunFilter;
import com.sdlctower.domain.aicenter.dto.SkillDetailDto;
import com.sdlctower.domain.aicenter.dto.SkillDto;
import com.sdlctower.domain.aicenter.dto.StageCoverageDto;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiCenterController {

    private final MetricsService metricsService;
    private final SkillService skillService;
    private final SkillExecutionService executionService;

    public AiCenterController(MetricsService metricsService,
                               SkillService skillService,
                               SkillExecutionService executionService) {
        this.metricsService = metricsService;
        this.skillService = skillService;
        this.executionService = executionService;
    }

    @GetMapping(ApiConstants.AI_CENTER_METRICS)
    public ApiResponse<MetricsSummaryDto> metrics(
            @RequestHeader("X-Workspace-Id") String workspaceId,
            @RequestParam(defaultValue = "30d") String window) {
        return ApiResponse.ok(metricsService.summarize(workspaceId, window));
    }

    @GetMapping(ApiConstants.AI_CENTER_STAGE_COVERAGE)
    public ApiResponse<StageCoverageDto> stageCoverage(
            @RequestHeader("X-Workspace-Id") String workspaceId) {
        return ApiResponse.ok(metricsService.stageCoverage(workspaceId));
    }

    @GetMapping(ApiConstants.AI_CENTER_SKILLS)
    public ApiResponse<List<SkillDto>> skills(
            @RequestHeader("X-Workspace-Id") String workspaceId) {
        return ApiResponse.ok(skillService.list(workspaceId));
    }

    @GetMapping(ApiConstants.AI_CENTER_SKILL_DETAIL)
    public ApiResponse<SkillDetailDto> skillDetail(
            @RequestHeader("X-Workspace-Id") String workspaceId,
            @PathVariable String skillKey) {
        return ApiResponse.ok(skillService.detail(workspaceId, skillKey));
    }

    @GetMapping(ApiConstants.AI_CENTER_RUNS)
    public ApiResponse<PageDto<RunDto>> runs(
            @RequestHeader("X-Workspace-Id") String workspaceId,
            @RequestParam(required = false) List<String> skillKey,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String triggerSourcePage,
            @RequestParam(required = false) Instant startedAfter,
            @RequestParam(required = false) Instant startedBefore,
            @RequestParam(required = false) String triggeredByType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (size < 1 || size > 200) {
            throw new IllegalArgumentException("size must be between 1 and 200");
        }
        RunFilter filter = new RunFilter(skillKey, status, triggerSourcePage,
                startedAfter, startedBefore, triggeredByType);
        return ApiResponse.ok(executionService.page(workspaceId, filter, page, size));
    }

    @GetMapping(ApiConstants.AI_CENTER_RUN_DETAIL)
    public ApiResponse<RunDetailDto> runDetail(
            @RequestHeader("X-Workspace-Id") String workspaceId,
            @PathVariable String executionId) {
        return ApiResponse.ok(executionService.detail(workspaceId, executionId));
    }
}
