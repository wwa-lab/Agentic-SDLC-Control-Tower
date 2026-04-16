package com.sdlctower.domain.teamspace.dto;

import java.time.Instant;

public record LineageHopDto(String origin, String value, Instant setAt, String setBy) {}
