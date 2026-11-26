package com.azhi.mushoku.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** The hand pass uses a distinct FOV and applies camera bob before rendering both hands. */
@Mixin(GameRenderer.class)
public interface HandCameraAccess {
    @Invoker("getFov") double mushokuHandFov(Camera camera, float partial, boolean useSetting);
    @Invoker("bobView") void mushokuBobView(PoseStack pose, float partial);
    @Invoker("bobHurt") void mushokuBobHurt(PoseStack pose, float partial);
}
