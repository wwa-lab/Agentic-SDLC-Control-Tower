package com.sdlctower.domain.teamspace.dto;

public record TemplateEntryDto(
        String id,
        String name,
        String version,
        String kind,
        LineageDto lineage,
        String detailLink
) {}
