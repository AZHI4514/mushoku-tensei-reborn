package com.azhi.mushoku.magic.core;

import org.junit.jupiter.api.Test;

import static com.azhi.mushoku.magic.core.Chant.Result.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChantTest {
    private final Chant chant = new Chant();

    private void start() {
        assertEquals(STARTED, chant.start(1, false, true));
        assertEquals(100, chant.remainingTicks());
    }

    private void advanceWithoutCompletion(int count) {
        for (int i = 0; i < count; i++) {
            assertEquals(NONE, chant.tick(1));
        }
    }

    @Test
    void spectatorCannotStart() {
        assertEquals(INELIGIBLE, chant.start(100, true, true));
        assertEquals(0, chant.remainingTicks());
    }

    @Test
    void deadCasterCannotStart() {
        assertEquals(INELIGIBLE, chant.start(100, false, false));
        assertEquals(0, chant.remainingTicks());
    }

    @Test
    void insufficientManaCannotStart() {
        assertEquals(INSUFFICIENT_MANA, chant.start(0, false, true));
        assertEquals(0, chant.remainingTicks());
    }

    @Test
    void duplicateStartPreservesProgress() {
        start();
        advanceWithoutCompletion(17);
        assertEquals(NONE, chant.start(1, false, true));
        assertEquals(83, chant.remainingTicks());
    }

    @Test
    void tick99StillChanting() {
        start();
        advanceWithoutCompletion(99);
        assertEquals(1, chant.remainingTicks());
    }

    @Test
    void tick100AuthorizesExactlyOneDebit() {
        start();
        advanceWithoutCompletion(99);
        assertEquals(COMPLETED, chant.tick(1));
        assertEquals(1, Chant.COST);
        assertEquals(0, chant.remainingTicks());
        assertEquals(NONE, chant.tick(1));
        assertEquals(NONE, chant.interrupt());
        start();
    }

    @Test
    void interruptAfterTick99CancelsSettlement() {
        start();
        advanceWithoutCompletion(99);
        assertEquals(INTERRUPTED, chant.interrupt());
        assertEquals(0, chant.remainingTicks());
        assertEquals(NONE, chant.tick(1));
        assertEquals(NONE, chant.interrupt());
        start();
    }

    @Test
    void insufficientManaAtSettlementReturnsToIdle() {
        start();
        advanceWithoutCompletion(99);
        assertEquals(INSUFFICIENT_MANA, chant.tick(0));
        assertEquals(0, chant.remainingTicks());
        assertEquals(NONE, chant.tick(100));
        start();
    }
}
