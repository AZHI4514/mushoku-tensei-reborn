package com.azhi.mushoku.magic.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.azhi.mushoku.magic.core.CastGeometry.Vector;

class CastGeometryTest {
    @Test void raisedPalmIsHorizontalAndTurnsWithTheBody() {
        var south = CastGeometry.palmOffset(0, 1, 0, false);
        var west = CastGeometry.palmOffset(90, 1, 0, false);
        assertTrue(south.x() < 0 && south.z() > 0);
        assertEquals(south.x(), west.z(), 1e-10);
        assertEquals(-south.z(), west.x(), 1e-10);
        assertEquals(CastGeometry.PLAYER_MODEL_SCALE * (CastGeometry.MODEL_ORIGIN - 2.0 / 16), south.y(), 1e-7);
        assertEquals(south, CastGeometry.palmOffset(0, 1, -0.8f, false));
        assertTrue(CastGeometry.palmOffset(0, 0, 0, false).y() < south.y());
    }

    @Test void nearTransitionAndConvergence() {
        for (double pitch : new double[]{-90, -30, 0, 30, 90}) {
            var look = new Vector(0, -Math.sin(Math.toRadians(pitch)), Math.cos(Math.toRadians(pitch)));
            var hand = CastGeometry.palmOffset(0, 1, 0, false).plus(new Vector(0, -1.62, 0));
            for (double d : new double[]{2.9,3,3.1,5,12,24}) {
                var toTarget = look.times(d).plus(hand.times(-1));
                var dir = CastGeometry.aim(look,toTarget,d);
                assertEquals(1, dir.length(), 1e-12);
                if (d<=3) assertEquals(look,dir);
                if (d==3.1) assertTrue(Math.toDegrees(Math.acos(Math.clamp(dir.y()*look.y()+dir.z()*look.z(), -1, 1)))<=5);
                if (d>=5) assertEquals(0, dir.plus(toTarget.unit().times(-1)).length(), 1e-12);
            }
        }
    }
}
