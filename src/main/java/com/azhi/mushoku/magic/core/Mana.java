package com.azhi.mushoku.magic.core;

public final class Mana {
    public static final int MAX = 100;

    private Mana() {}

    public static int clamp(int current) {
        return Math.clamp(current, 0, MAX);
    }

    public static int spend(int current, int amount) {
        requireNonNegative(amount);
        return Math.max(0, clamp(current) - amount);
    }

    public static int restore(int current, int amount) {
        requireNonNegative(amount);
        return Math.clamp((long) clamp(current) + amount, 0, MAX);
    }

    private static void requireNonNegative(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Mana amount must be non-negative");
        }
    }
}
