package com.sdlctower.domain.projectspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProjectAccessGuardTest {

    private final ProjectAccessGuard guard = new ProjectAccessGuard(new ProjectSpaceSeedCatalog());

    @Test
    void allowsReadableProject() {
        assertDoesNotThrow(() -> guard.check("proj-42"));
    }

    @Test
    void rejectsDeniedProject() {
        assertThrows(ProjectAccessDeniedException.class, () -> guard.check("proj-private-01"));
    }

    @Test
    void rejectsInvalidPattern() {
        assertThrows(IllegalArgumentException.class, () -> guard.check("INVALID"));
    }
}
