package com.sdlctower.domain.aicenter.policy;

import com.sdlctower.shared.exception.ResourceNotFoundException;

public class SkillExecutionNotFoundException extends ResourceNotFoundException {

    public SkillExecutionNotFoundException(String executionId) {
        super("Skill execution not found: " + executionId);
    }
}
