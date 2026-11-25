package com.azhi.mushoku.dev;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.runtime.CastAnchor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Mushoku.MOD_ID, value = Dist.CLIENT)
public final class PalmSampler extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static Matrix4f inverseEntityPose;
    private static int renderedPlayer;
    private final java.util.Map<Integer, Long> lastTick = new java.util.HashMap<>();

    private PalmSampler(PlayerRenderer renderer) { super(renderer); }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            renderer.addLayer(new PalmSampler(renderer));
        }
    }

    @SubscribeEvent
    public static void pre(RenderPlayerEvent.Pre event) {
        renderedPlayer = event.getEntity().getId();
        inverseEntityPose = new Matrix4f(event.getPoseStack().last().pose()).invert();
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer p,
                       float swing, float amplitude, float partial, float age, float yaw, float pitch) {
        long tick = p.level().getGameTime();
        if (renderedPlayer != p.getId() || Long.valueOf(tick).equals(lastTick.put(p.getId(), tick))) return;
        var arm = p.getMainArm() == HumanoidArm.RIGHT ? getParentModel().rightArm : getParentModel().leftArm;
        var cube = arm.getRandomCube(net.minecraft.util.RandomSource.create(0));
        pose.pushPose();
        arm.translateAndRotate(pose);
        var end = new Vector3f((cube.minX + cube.maxX) / 32, cube.maxY / 16, (cube.minZ + cube.maxZ) / 32);
        new Matrix4f(inverseEntityPose).mul(pose.last().pose()).transformPosition(end);
        pose.popPose();
        Vec3 palm = p.getPosition(partial).add(end.x, end.y, end.z);
        Vec3 hand = CastAnchor.hand(p, partial);
        AnchorSamples.LOG.info("ANCHOR palm player={} tick={} partial={} yaw={} pitch={} main={} phase={} amp={} pos={} palm={} H={} error={}",
                p.getScoreboardName(), tick, partial, p.getViewYRot(partial), p.getViewXRot(partial), p.getMainArm(),
                swing, amplitude, p.getPosition(partial), palm, hand, palm.distanceTo(hand));
    }
}
