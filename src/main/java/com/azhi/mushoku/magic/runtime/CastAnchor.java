package com.azhi.mushoku.magic.runtime;

import com.azhi.mushoku.magic.core.CastGeometry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

public final class CastAnchor {
    private CastAnchor() {}

    public static Vec3 hand(Player caster) { return hand(caster, 1); }

    public static Vec3 hand(Player caster, float partialTick) {
        var offset = CastGeometry.handOffset(vector(caster.getViewVector(partialTick)),
                caster.getViewYRot(partialTick), caster.getMainArm() == HumanoidArm.RIGHT);
        return caster.getEyePosition(partialTick).add(offset.x(), offset.y(), offset.z());
    }

    static CastGeometry.Vector vector(Vec3 v) { return new CastGeometry.Vector(v.x, v.y, v.z); }
}
