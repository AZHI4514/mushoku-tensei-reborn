package com.azhi.mushoku.client;

import com.azhi.mushoku.magic.core.CastGeometry;
import com.azhi.mushoku.magic.runtime.CastAnchor;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;

public final class CastingArms {
    private CastingArms() {}

    public static void apply(PlayerModel<?> model, AbstractClientPlayer player, float partial, float weight) {
        var right = model.rightArm;
        var left = model.leftArm;
        right.xRot = CastGeometry.rightArmPitch(weight, CastAnchor.restingPitch(player, partial));
        right.yRot = CastGeometry.RIGHT_TURN * weight;
        right.zRot = 0;
        // Slim arms have their palm centre half a model unit closer to the shoulder.
        boolean slim = player.getSkin().model() == PlayerSkin.Model.SLIM;
        float correction = slim ? -0.5f : 0;
        right.setPos(-5 + correction * Mth.cos(right.yRot), player.isCrouching() ? 5.2f : 2,
                -correction * Mth.sin(right.yRot));
        left.xRot = Mth.lerp(weight, left.xRot, -Mth.HALF_PI);
        left.yRot = Mth.lerp(weight, left.yRot, 0.91f);
        left.zRot *= 1 - weight;
        model.rightSleeve.copyFrom(right);
        model.leftSleeve.copyFrom(left);
    }
}
