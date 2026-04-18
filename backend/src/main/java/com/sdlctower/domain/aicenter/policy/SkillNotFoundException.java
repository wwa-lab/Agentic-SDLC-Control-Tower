package com.sdlctower.domain.aicenter.policy;

import com.sdlctower.shared.exception.ResourceNotFoundException;

public class SkillNotFoundException extends ResourceNotFoundException {

    public SkillNotFoundException(String skillKey) {
        super("Skill not found: " + skillKey);
    }
}
