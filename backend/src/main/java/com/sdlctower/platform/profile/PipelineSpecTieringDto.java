package com.sdlctower.platform.profile;

import java.util.List;

public record PipelineSpecTieringDto(
        List<String> tiers,
        String defaultTier
) {}
