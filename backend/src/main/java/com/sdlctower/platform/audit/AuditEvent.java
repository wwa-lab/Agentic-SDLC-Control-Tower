package com.sdlctower.platform.audit;

public record AuditEvent(
        String actor,
        String actorType,
        String category,
        String action,
        String objectType,
        String objectId,
        String scopeType,
        String scopeId,
        String outcome,
        String evidenceRef,
        Object payload
) {}
