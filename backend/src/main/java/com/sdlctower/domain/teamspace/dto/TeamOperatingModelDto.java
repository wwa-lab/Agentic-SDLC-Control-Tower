package com.sdlctower.domain.teamspace.dto;

import java.util.List;

public record TeamOperatingModelDto(
        FieldDto<String> operatingMode,
        FieldDto<String> approvalMode,
        FieldDto<String> aiAutonomyLevel,
        FieldDto<OncallOwnerDto> oncallOwner,
        List<AccountableOwnerDto> accountableOwners,
        LinkDto platformCenterLink
) {}
