package com.sdlctower.domain.projectspace.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnvironmentMatrixProjectionTest {

    @Test
    void resolveDriftBandUsesExpectedThresholds() {
        assertEquals("NONE", EnvironmentMatrixProjection.resolveDriftBand(0));
        assertEquals("MINOR", EnvironmentMatrixProjection.resolveDriftBand(10));
        assertEquals("MAJOR", EnvironmentMatrixProjection.resolveDriftBand(11));
    }
}
