package com.azhi.mushoku.magic.runtime;

import com.azhi.mushoku.magic.core.CastGeometry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

public final class CastAnchor {
    private CastAnchor() {}

    public static float restingPitch(Player caster, float partial) {
        float swing = caster.walkAnimation.position(partial);
        float amplitude = Math.min(1, caster.walkAnimation.speed(partial));
        float angle = Mth.cos(swing * 0.6662f + Mth.PI) * amplitude;
        var held = caster.getMainArm() == HumanoidArm.RIGHT ? caster.getMainHandItem() : caster.getOffhandItem();
        return held.isEmpty() ? angle : angle * 0.5f - Mth.PI / 10;
    }

    public static Vec3 hand(Player caster, float partial, float weight) {
        float yaw = Mth.rotLerp(partial, caster.yBodyRotO, caster.yBodyRot);
        var offset = CastGeometry.palmOffset(yaw, weight, restingPitch(caster, partial), caster.isCrouching())
                .times(caster.getScale());
        return caster.getPosition(partial).add(offset.x(), offset.y(), offset.z());
    }

    static CastGeometry.Vector vector(Vec3 v) { return new CastGeometry.Vector(v.x, v.y, v.z); }
}
