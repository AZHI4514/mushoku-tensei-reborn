package com.azhi.mushoku.magic.core;

/** One instance per caster. Completion authorizes one mana debit by the caller. */
public final class Chant {
    public static final int DURATION_TICKS = 100;
    public static final int COST = 1;

    public enum Result {
        NONE, STARTED, INTERRUPTED, COMPLETED, INSUFFICIENT_MANA, INELIGIBLE
    }

    // Zero means idle, including immediately after a terminal transition.
    private int remainingTicks;

    public int remainingTicks() {
        return remainingTicks;
    }

    public Result start(int mana, boolean spectator, boolean alive) {
        if (remainingTicks > 0) return Result.NONE;
        if (spectator || !alive) return Result.INELIGIBLE;
        if (mana < COST) return Result.INSUFFICIENT_MANA;
        remainingTicks = DURATION_TICKS;
        return Result.STARTED;
    }

    public Result interrupt() {
        if (remainingTicks == 0) return Result.NONE;
        remainingTicks = 0;
        return Result.INTERRUPTED;
    }

    /** Advance exactly one server game tick using the caster's current balance. */
    public Result tick(int mana) {
        if (remainingTicks == 0) return Result.NONE;
        remainingTicks--;
        if (remainingTicks > 0) return Result.NONE;
        return mana >= COST ? Result.COMPLETED : Result.INSUFFICIENT_MANA;
    }
}
