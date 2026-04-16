package com.sdlctower.domain.teamspace.dto;

import java.util.List;
import java.util.Map;

public record TeamDefaultTemplatesDto(
        Map<String, List<TemplateEntryDto>> groups,
        List<ExceptionOverrideDto> exceptionOverrides
) {}
