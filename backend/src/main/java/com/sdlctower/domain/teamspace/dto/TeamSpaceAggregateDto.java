package com.sdlctower.domain.teamspace.dto;

import com.sdlctower.shared.dto.SectionResultDto;

public record TeamSpaceAggregateDto(
        String workspaceId,
        SectionResultDto<WorkspaceSummaryDto> summary,
        SectionResultDto<TeamOperatingModelDto> operatingModel,
        SectionResultDto<MemberMatrixDto> members,
        SectionResultDto<TeamDefaultTemplatesDto> templates,
        SectionResultDto<RequirementPipelineDto> pipeline,
        SectionResultDto<TeamMetricsDto> metrics,
        SectionResultDto<TeamRiskRadarDto> risks,
        SectionResultDto<ProjectDistributionDto> projects
) {}
