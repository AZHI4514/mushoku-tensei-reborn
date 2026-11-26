package com.azhi.mushoku.magic.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CastingPoseTest {
    @Test void reachesFullHeightAtFiveTicks() {
        assertEquals(0f, CastingPose.lift(0));
        assertEquals(.8f, CastingPose.lift(4));
        assertEquals(1f, CastingPose.lift(5));
        assertEquals(1f, CastingPose.lift(100));
    }

    @Test void interruptedLiftLowersFromItsCurrentHeight() {
        float start = CastingPose.lift(2);
        assertEquals(start, CastingPose.lower(start, 0));
        assertEquals(start / 2, CastingPose.lower(start, 2.5f));
        assertEquals(0f, CastingPose.lower(start, 5));
    }
}
