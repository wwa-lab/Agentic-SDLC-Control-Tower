package com.sdlctower.domain.teamspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WorkspaceAccessGuardTest {

    private final WorkspaceAccessGuard guard = new WorkspaceAccessGuard(new TeamSpaceSeedCatalog());

    @Test
    void allowsReadableWorkspace() {
        assertDoesNotThrow(() -> guard.check("ws-default-001"));
    }

    @Test
    void rejectsDeniedWorkspace() {
        assertThrows(WorkspaceAccessDeniedException.class, () -> guard.check("ws-private-001"));
    }

    @Test
    void rejectsInvalidPattern() {
        assertThrows(IllegalArgumentException.class, () -> guard.check("INVALID"));
    }
}
