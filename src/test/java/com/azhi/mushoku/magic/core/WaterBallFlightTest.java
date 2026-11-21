package com.azhi.mushoku.magic.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaterBallFlightTest {
    @Test
    void startsWithFullRangeAndNormalStep() {
        assertEquals(24.0, WaterBallFlight.remainingDistance(0));
        assertEquals(1.2, WaterBallFlight.nextStep(0));
    }

    @Test
    void clipsLastSegmentAtRangeEndpoint() {
        assertEquals(0.5, WaterBallFlight.nextStep(23.5));
        assertEquals(0.0, WaterBallFlight.nextStep(24));
        assertEquals(0.0, WaterBallFlight.remainingDistance(25));
    }

    @Test
    void normalFlightUsesExactlyTwentySteps() {
        double travelled = 0;
        for (int tick = 1; tick <= 20; tick++) {
            double step = WaterBallFlight.nextStep(travelled);
            assertTrue(step > 0 && step <= 1.2);
            travelled += step;
        }
        assertEquals(24.0, travelled, 1e-12);
        assertEquals(0.0, WaterBallFlight.nextStep(travelled));
    }

    @Test
    void stationaryFlightExpiresOnTickForty() {
        assertEquals(24.0, WaterBallFlight.remainingDistance(0));
        assertFalse(WaterBallFlight.expired(39));
        assertTrue(WaterBallFlight.expired(40));
        assertTrue(WaterBallFlight.expired(41));
    }
}
