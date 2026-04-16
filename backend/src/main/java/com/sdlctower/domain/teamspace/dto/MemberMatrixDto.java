package com.sdlctower.domain.teamspace.dto;

import java.util.List;

public record MemberMatrixDto(
        List<MemberMatrixRowDto> members,
        List<CoverageGapDto> coverageGaps,
        LinkDto accessManagementLink
) {}
