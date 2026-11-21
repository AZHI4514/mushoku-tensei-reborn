package com.azhi.mushoku.magic.core;

public final class WaterBallFlight {
    public static final double SPEED = 1.2;
    public static final double RANGE = 24.0;
    public static final int MAX_AGE_TICKS = 40;

    private WaterBallFlight() {}

    public static double remainingDistance(double travelled) {
        double remaining = RANGE - travelled;
        // Twenty additions of 1.2 can leave two ULPs at the endpoint.
        return remaining <= 2 * Math.ulp(RANGE) ? 0.0 : remaining;
    }

    public static double nextStep(double travelled) {
        return Math.min(SPEED, remainingDistance(travelled));
    }

    public static boolean expired(int ageTicks) {
        return ageTicks >= MAX_AGE_TICKS;
    }
}
