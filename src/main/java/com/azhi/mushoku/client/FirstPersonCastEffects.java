package com.azhi.mushoku.client;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.client.mixin.HandCameraAccess;
import com.azhi.mushoku.client.mixin.ParticleOriginAccess;
import com.azhi.mushoku.magic.runtime.WaterBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = Mushoku.MOD_ID, value = Dist.CLIENT)
public final class FirstPersonCastEffects {
    private static final float HANDOFF_TICKS = 3;
    private static final Map<WaterBallEntity, Launch> launches = new HashMap<>();
    private static final ArrayList<Particle> newborn = new ArrayList<>();
    private static boolean splashDue;

    private static final class Launch {
        Vec3 initialOffset;
        Vec3 offset = Vec3.ZERO;
        double firstFrame;
    }

    private FirstPersonCastEffects() {}

    static boolean localView() {
        var mc = Minecraft.getInstance();
        return mc.player != null && mc.getCameraEntity() == mc.player && mc.options.getCameraType().isFirstPerson()
                && !mc.options.hideGui && !mc.player.isSpectator();
    }

    static void requestSplash() {
        splashDue = true;
    }

    static void stopSplashes() {
        splashDue = false;
    }

    @SubscribeEvent
    public static void beforeTick(ClientTickEvent.Pre event) {
        if (!splashDue) return;
        splashDue = false;
        var mc = Minecraft.getInstance();
        if (!localView() || mc.isPaused() || !mc.player.isAlive()) return;
        // Queue before the vanilla particle tick so the first subsequent world pass can draw it.
        var eye = mc.player.getEyePosition();
        var drop = mc.particleEngine.createParticle(ParticleTypes.SPLASH, eye.x, eye.y, eye.z, 0, 0, 0);
        if (drop != null) newborn.add(drop);
    }

    @SubscribeEvent
    public static void joined(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof WaterBallEntity ball && event.getLevel().isClientSide()
                && localView() && ball.getOwner() == Minecraft.getInstance().player) {
            launches.put(ball, new Launch());
        }
    }

    @SubscribeEvent
    public static void left(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof WaterBallEntity ball) launches.remove(ball);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) clear();
    }

    private static void clear() {
        launches.clear();
        splashDue = false;
        newborn.forEach(Particle::remove);
        newborn.clear();
    }

    @SubscribeEvent
    public static void beforeEntities(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        if (!localView()) {
            clear();
            return;
        }
        if (newborn.isEmpty() && launches.isEmpty()) return;
        var mc = Minecraft.getInstance();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        Vec3 palm = projectedWorldPalm(event, partial);
        for (Particle drop : newborn) {
            drop.setPos(palm.x, palm.y, palm.z);
            var previous = (ParticleOriginAccess) drop;
            previous.mushokuPreviousX(palm.x);
            previous.mushokuPreviousY(palm.y);
            previous.mushokuPreviousZ(palm.z);
        }
        newborn.clear();
        double now = mc.level.getGameTime() + (double) partial;
        launches.entrySet().removeIf(entry -> {
            var ball = entry.getKey();
            var launch = entry.getValue();
            if (launch.initialOffset == null) {
                // EntityRenderDispatcher interpolates from xOld/yOld/zOld (initialised on first draw).
                var position = ball.tickCount == 0 ? ball.position() : new Vec3(
                        Mth.lerp(partial, ball.xOld, ball.getX()), Mth.lerp(partial, ball.yOld, ball.getY()),
                        Mth.lerp(partial, ball.zOld, ball.getZ()));
                launch.initialOffset = palm.subtract(position);
                launch.firstFrame = now;
            }
            double t = Mth.clamp((now - launch.firstFrame) / HANDOFF_TICKS, 0, 1);
            launch.offset = launch.initialOffset.scale(1 - t * t * (3 - 2 * t));
            return t >= 1;
        });
    }

    private static Vec3 projectedWorldPalm(RenderLevelStageEvent event, float partial) {
        var mc = Minecraft.getInstance();
        var camera = event.getCamera();
        var handCamera = (HandCameraAccess) mc.gameRenderer;
        PoseStack bob = new PoseStack();
        handCamera.mushokuBobHurt(bob, partial);
        if (mc.options.bobView().get()) handCamera.mushokuBobView(bob, partial);
        var handProjection = mc.gameRenderer.getProjectionMatrix(handCamera.mushokuHandFov(camera, partial, false));
        var clip = handProjection.mul(bob.last().pose()).transformProject(
                FirstPersonCastingArms.cameraPalm(mc.player, partial));
        var inverseWorld = new Matrix4f(event.getProjectionMatrix()).mul(event.getModelViewMatrix()).invert();
        inverseWorld.transformProject(clip);
        return camera.getPosition().add(clip.x, clip.y, clip.z);
    }

    static boolean presenting(WaterBallEntity ball) {
        return launches.containsKey(ball);
    }

    static Vec3 offset(WaterBallEntity ball) {
        var launch = launches.get(ball);
        return launch == null ? Vec3.ZERO : launch.offset;
    }
}
