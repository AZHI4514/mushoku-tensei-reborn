package com.azhi.mushoku.client.mixin;

import com.azhi.mushoku.client.CastingArms;
import com.azhi.mushoku.client.ClientChant;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
abstract class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void mushokuCastingArms(LivingEntity entity, float swing, float speed, float age,
                                    float yaw, float pitch, CallbackInfo info) {
        if (!(entity instanceof AbstractClientPlayer player)) return;
        float partial = Math.clamp(age - player.tickCount, 0, 1);
        float weight = ClientChant.weight(player, partial);
        if (weight > 0) CastingArms.apply((PlayerModel<?>)(Object)this, player, partial, weight);
    }
}
