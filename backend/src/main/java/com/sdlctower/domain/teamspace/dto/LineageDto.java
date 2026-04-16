package com.sdlctower.domain.teamspace.dto;

import java.util.List;

public record LineageDto(String origin, boolean overridden, List<LineageHopDto> chain) {}
