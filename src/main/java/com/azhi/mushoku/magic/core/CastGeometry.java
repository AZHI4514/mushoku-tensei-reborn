package com.azhi.mushoku.magic.core;

public final class CastGeometry {
    private CastGeometry() {}

    public record Vector(double x, double y, double z) {
        public Vector plus(Vector v) { return new Vector(x + v.x, y + v.y, z + v.z); }
        public Vector times(double k) { return new Vector(k * x, k * y, k * z); }
        public double length() { return Math.sqrt(x * x + y * y + z * z); }
        public Vector unit() { return times(1 / length()); }
    }

    public static final double PLAYER_MODEL_SCALE = 0.9375;
    public static final double MODEL_ORIGIN = 1.501;
    public static final float RIGHT_TURN = -0.38f;

    public static float rightArmPitch(float weight, float restingPitch) {
        return (float) (restingPitch + weight * (-Math.PI / 2 - restingPitch));
    }

    /** Right palm relative to the feet, following the vanilla model's Y-X arm rotations. */
    public static Vector palmOffset(double bodyYaw, float weight, float restingPitch, boolean crouching) {
        double pitch = rightArmPitch(weight, restingPitch);
        double turn = RIGHT_TURN * weight;
        double z = 10 * Math.sin(pitch);
        double modelX = -5 - Math.cos(turn) + z * Math.sin(turn);
        double modelZ = Math.sin(turn) + z * Math.cos(turn);
        double modelY = (crouching ? 5.2 : 2) + 10 * Math.cos(pitch);
        double yaw = Math.toRadians(bodyYaw);
        double unit = PLAYER_MODEL_SCALE / 16;
        return new Vector(unit * (modelX * Math.cos(yaw) + modelZ * Math.sin(yaw)),
                PLAYER_MODEL_SCALE * MODEL_ORIGIN - unit * modelY - (crouching ? 0.125 : 0),
                unit * (modelX * Math.sin(yaw) - modelZ * Math.cos(yaw)));
    }

    public static Vector aim(Vector look, Vector handToTarget, double eyeDistance) {
        if (eyeDistance <= 3) return look;
        Vector direct = handToTarget.unit();
        double t = Math.min(1, (eyeDistance - 3) / 2);
        double weight = t * t * (3 - 2 * t);
        return look.times(1 - weight).plus(direct.times(weight)).unit();
    }
}
