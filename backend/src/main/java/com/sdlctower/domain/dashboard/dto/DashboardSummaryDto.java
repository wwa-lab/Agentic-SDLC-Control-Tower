package com.sdlctower.domain.dashboard.dto;

import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;

public record DashboardSummaryDto(
        SectionResultDto<List<SdlcStageHealthDto>> sdlcHealth,
        SectionResultDto<DeliveryMetricsDto> deliveryMetrics,
        SectionResultDto<AiParticipationDto> aiParticipation,
        SectionResultDto<QualityMetricsDto> qualityMetrics,
        SectionResultDto<StabilityMetricsDto> stabilityMetrics,
        SectionResultDto<GovernanceMetricsDto> governanceMetrics,
        SectionResultDto<RecentActivityDto> recentActivity,
        SectionResultDto<ValueStoryDto> valueStory
) {
}
