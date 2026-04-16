package com.sdlctower.domain.requirement.dto;

import java.util.List;

public record CreateRequirementRequestDto(
        String title,
        String priority,
        String category,
        String summary,
        String businessJustification,
        List<String> acceptanceCriteria,
        List<String> assumptions,
        List<String> constraints,
        RawRequirementInputDto sourceAttachment
) {}
