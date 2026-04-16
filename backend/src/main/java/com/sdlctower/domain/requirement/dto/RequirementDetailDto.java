package com.sdlctower.domain.requirement.dto;

import com.sdlctower.shared.dto.SectionResultDto;

public record RequirementDetailDto(
        SectionResultDto<RequirementHeaderDto> header,
        SectionResultDto<RequirementDescriptionDto> description,
        SectionResultDto<LinkedStoriesSectionDto> linkedStories,
        SectionResultDto<LinkedSpecsSectionDto> linkedSpecs,
        SectionResultDto<AiAnalysisDto> aiAnalysis,
        SectionResultDto<SdlcChainDto> sdlcChain
) {}
