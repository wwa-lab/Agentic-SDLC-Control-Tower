package com.sdlctower.domain.incident.dto;

import com.sdlctower.shared.dto.SectionResultDto;

public record IncidentDetailDto(
        SectionResultDto<IncidentHeaderDto> header,
        SectionResultDto<DiagnosisFeedDto> diagnosis,
        SectionResultDto<SkillTimelineDto> skillTimeline,
        SectionResultDto<IncidentActionsDto> actions,
        SectionResultDto<GovernanceDto> governance,
        SectionResultDto<SdlcChainDto> sdlcChain,
        SectionResultDto<AiLearningDto> learning
) {}
