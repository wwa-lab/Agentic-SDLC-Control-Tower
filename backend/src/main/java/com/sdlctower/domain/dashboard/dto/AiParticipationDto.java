package com.sdlctower.domain.dashboard.dto;

import java.util.List;

public record AiParticipationDto(
        MetricValueDto usageRate,
        MetricValueDto adoptionRate,
        MetricValueDto autoExecSuccess,
        MetricValueDto timeSaved,
        List<AiInvolvementDto> stageInvolvement
) {
}
