package com.azhi.mushoku.magic.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.azhi.mushoku.magic.core.CastGeometry.Vector;

class CastGeometryTest {
    @Test void mirrorStaysHorizontalAtEveryPitch() {
        for (double yaw : new double[]{0, 45, 180}) {
            double y = Math.toRadians(yaw);
            for (double pitch : new double[]{-90, -89.999, -30, 0, 30, 89.999, 90}) {
                double p = Math.toRadians(pitch);
                var look = new Vector(-Math.sin(y)*Math.cos(p), -Math.sin(p), Math.cos(y)*Math.cos(p));
                var right = CastGeometry.handOffset(look, yaw, true);
                var left = CastGeometry.handOffset(look, yaw, false);
                var delta = right.plus(left.times(-1));
                assertEquals(0, delta.y(), 1e-12);
                assertEquals(0.74, delta.length(), 1e-9);
                assertEquals(0, delta.x()*look.x()+delta.z()*look.z(), 1e-9);
                assertEquals(right, CastGeometry.handOffset(look, yaw, true));
            }
        }
    }

    @Test void nearTransitionAndConvergence() {
        var look = new Vector(0, 0, 1);
        for (boolean right : new boolean[]{true,false}) {
            var hand = CastGeometry.handOffset(look, 0, right);
            for (double d : new double[]{2.9,3,3.1,5,12,24}) {
                var toTarget = look.times(d).plus(hand.times(-1));
                var dir = CastGeometry.aim(look,toTarget,d);
                assertEquals(1, dir.length(), 1e-12);
                if (d<=3) assertEquals(look,dir);
                if (d==3.1) assertTrue(Math.toDegrees(Math.acos(dir.z()))<=5);
                if (d>=5) assertEquals(0, dir.plus(toTarget.unit().times(-1)).length(), 1e-12);
            }
        }
    }
}
