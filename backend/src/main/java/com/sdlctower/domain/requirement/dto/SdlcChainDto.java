package com.sdlctower.domain.requirement.dto;

import java.util.List;

public record SdlcChainDto(
        List<SdlcChainLinkDto> links
) {}
