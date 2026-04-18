package com.sdlctower.domain.aicenter.dto;

import java.util.List;
import java.util.Map;

public record RunDetailDto(
        RunDto run,
        Map<String, Object> inputSummary,
        Map<String, Object> outputSummary,
        List<RunStepDto> stepBreakdown,
        List<PolicyTrailEntryDto> policyTrail,
        List<EvidenceLinkDto> evidenceLinks,
        String autonomyLevel,
        int timeSavedMinutes
) {}
