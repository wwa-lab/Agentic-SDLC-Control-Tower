package com.sdlctower.platform.template;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public record InheritanceFieldDto(
        JsonNode effectiveValue,
        String winningLayer,
        Map<String, JsonNode> layers
) {}
