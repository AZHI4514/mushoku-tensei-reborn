package com.azhi.mushoku.magic.core;

/** Time is measured in simulation ticks, including fractional render ticks. */
public final class CastingPose {
    public static final int TRANSITION_TICKS = 5;

    private CastingPose() {}

    public static float lift(float elapsed) {
        return Math.clamp(elapsed / TRANSITION_TICKS, 0f, 1f);
    }

    public static float lower(float initialWeight, float elapsed) {
        return initialWeight * (1 - lift(elapsed));
    }
}
