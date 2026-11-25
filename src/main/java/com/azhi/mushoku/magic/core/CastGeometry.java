package com.azhi.mushoku.magic.core;

public final class CastGeometry {
    private CastGeometry() {}

    public record Vector(double x, double y, double z) {
        public Vector plus(Vector v) { return new Vector(x + v.x, y + v.y, z + v.z); }
        public Vector times(double k) { return new Vector(k * x, k * y, k * z); }
        public double length() { return Math.sqrt(x * x + y * y + z * z); }
        public Vector unit() { return times(1 / length()); }
    }

    public static Vector handOffset(Vector forward, double yawDegrees, boolean rightHanded) {
        double yaw = Math.toRadians(yawDegrees);
        Vector right = new Vector(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vector up = new Vector(right.y * forward.z - right.z * forward.y,
                right.z * forward.x - right.x * forward.z, right.x * forward.y - right.y * forward.x);
        // The shoulder hangs below the eye even when the player looks up.
        return right.times(rightHanded ? 0.37 : -0.37).plus(new Vector(0, -0.82, 0))
                .plus(up.times(-0.06)).plus(forward.times(0.07));
    }

    public static Vector aim(Vector look, Vector handToTarget, double eyeDistance) {
        if (eyeDistance <= 3) return look;
        Vector direct = handToTarget.unit();
        double t = Math.min(1, (eyeDistance - 3) / 2);
        double weight = t * t * (3 - 2 * t);
        return look.times(1 - weight).plus(direct.times(weight)).unit();
    }
}
