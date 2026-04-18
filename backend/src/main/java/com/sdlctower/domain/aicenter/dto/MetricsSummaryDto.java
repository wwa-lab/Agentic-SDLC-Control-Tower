package com.sdlctower.domain.aicenter.dto;

import com.sdlctower.shared.dto.SectionResultDto;

public record MetricsSummaryDto(
        String window,
        SectionResultDto<MetricValueDto> aiUsageRate,
        SectionResultDto<MetricValueDto> adoptionRate,
        SectionResultDto<MetricValueDto> autoExecSuccessRate,
        SectionResultDto<MetricValueDto> timeSavedHours,
        SectionResultDto<MetricValueDto> stageCoverageCount
) {}
