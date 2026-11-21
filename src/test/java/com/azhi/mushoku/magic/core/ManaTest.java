package com.azhi.mushoku.magic.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManaTest {
    @Test
    void clampsToZeroAndOneHundred() {
        assertEquals(0, Mana.clamp(-1));
        assertEquals(100, Mana.clamp(101));
        assertEquals(37, Mana.clamp(37));
    }

    @Test
    void spendsAndRestoresWithinRange() {
        assertEquals(30, Mana.spend(37, 7));
        assertEquals(44, Mana.restore(37, 7));
        assertEquals(37, Mana.spend(37, 0));
        assertEquals(37, Mana.restore(37, 0));
    }

    @Test
    void spendingStopsAtZero() {
        assertEquals(0, Mana.spend(3, 5));
    }

    @Test
    void restorationStopsAtMaximum() {
        assertEquals(100, Mana.restore(98, 5));
    }

    @Test
    void maximumAmountsDoNotWrap() {
        assertEquals(0, Mana.spend(3, Integer.MAX_VALUE));
        assertEquals(100, Mana.restore(98, Integer.MAX_VALUE));
    }

    @Test
    void clampsCurrentBeforeArithmetic() {
        assertEquals(93, Mana.spend(Integer.MAX_VALUE, 7));
        assertEquals(7, Mana.restore(Integer.MIN_VALUE, 7));
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> Mana.spend(37, -1));
        assertThrows(IllegalArgumentException.class, () -> Mana.restore(37, -1));
    }
}
