package com.azhi.mushoku.client;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.runtime.WaterBallEntity;
import com.azhi.mushoku.registry.ModEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@EventBusSubscriber(modid = Mushoku.MOD_ID, value = Dist.CLIENT)
public final class WaterBallGeoRenderer extends GeoEntityRenderer<WaterBallEntity> {
    private WaterBallGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new WaterBallModel());
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WATER_BALL.get(), WaterBallGeoRenderer::new);
    }

    @Override
    public boolean shouldRender(WaterBallEntity ball, Frustum frustum, double x, double y, double z) {
        return FirstPersonCastEffects.presenting(ball) || super.shouldRender(ball, frustum, x, y, z);
    }

    @Override
    public Vec3 getRenderOffset(WaterBallEntity ball, float partial) {
        return super.getRenderOffset(ball, partial).add(FirstPersonCastEffects.offset(ball));
    }

    @Override
    protected void applyRotations(WaterBallEntity ball, PoseStack pose, float age, float bodyYaw,
                                  float partialTick, float scale) {
        Vec3 motion = ball.getDeltaMovement();
        if (motion.lengthSqr() != 0) {

            // Map the model's local -Z axis onto the synchronized flight vector.
            pose.mulPose(Axis.YP.rotation((float) Math.atan2(-motion.x, -motion.z)));
            pose.mulPose(Axis.XP.rotation((float) Math.atan2(motion.y, motion.horizontalDistance())));
        }
        // GeckoLib 4.7.7 adds this local Y offset after applyRotations.
        pose.translate(0, -0.01F, 0);
    }
}
