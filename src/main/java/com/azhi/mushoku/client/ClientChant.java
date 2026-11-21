package com.azhi.mushoku.client;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.runtime.ChantPackets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Mushoku.MOD_ID, value = Dist.CLIENT)
public final class ClientChant {
    private static final KeyMapping KEY = new KeyMapping("key.mushoku_tensei_reborn.chant",
            GLFW.GLFW_KEY_R, "key.categories.gameplay");
    private record Display(long receivedAt, int remaining, int total) {}
    private static final Map<Integer, Display> CASTERS = new HashMap<>();
    private static ClientLevel level;
    private static boolean held;
    private static boolean needsRelease;

    private ClientChant() {}

    private static void useLevel(ClientLevel current) {
        if (level != current) {
            level = current;
            CASTERS.clear();
            needsRelease = KEY.isDown();
            held = false;
        }
    }

    @SubscribeEvent
    public static void receive(ChantPackets.StateReceived event) {
        var state = event.state;
        useLevel(Minecraft.getInstance().level);
        if (level == null) return;
        if (state.phase() == ChantPackets.Phase.CHANTING) {
            CASTERS.put(state.casterId(), new Display(level.getGameTime(), state.remainingTicks(), state.totalTicks()));
        } else {
            CASTERS.remove(state.casterId());
        }
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(KEY);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        useLevel(mc.level);
        if (level == null || mc.player == null) return;
        boolean down = KEY.isDown() && mc.screen == null;
        if (!KEY.isDown()) needsRelease = false;
        boolean pressed = down && !needsRelease && mc.player.isAlive();
        if (pressed != held) {
            if (pressed) PacketDistributor.sendToServer(new ChantPackets.Start());
            else PacketDistributor.sendToServer(new ChantPackets.Interrupt());
            held = pressed;
        }
        if (mc.isPaused()) return;
        var staleIds = new ArrayList<Integer>();
        CASTERS.forEach((id, display) -> {
            if (!(level.getEntity(id) instanceof Player caster)) {
                staleIds.add(id);
                return;
            }
            double elapsed = display.total - display.remaining + level.getGameTime() - display.receivedAt;
            double progress = Math.clamp(elapsed / display.total, 0.0, 1.0);
            double yaw = Math.toRadians(caster.yBodyRot);
            double side = caster.getMainArm() == HumanoidArm.RIGHT ? -0.32 : 0.32;
            double reach = 0.25 + 0.12 * progress;
            level.addParticle(ParticleTypes.SPLASH,
                    caster.getX() + Math.cos(yaw) * side - Math.sin(yaw) * reach,
                    caster.getEyeY() - 0.55,
                    caster.getZ() + Math.sin(yaw) * side + Math.cos(yaw) * reach, 0, 0, 0);
        });
        staleIds.forEach(CASTERS::remove);
    }
}
