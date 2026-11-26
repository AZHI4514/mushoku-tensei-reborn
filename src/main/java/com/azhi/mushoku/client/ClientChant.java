package com.azhi.mushoku.client;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.runtime.CastAnchor;
import com.azhi.mushoku.magic.runtime.ChantPackets;
import com.azhi.mushoku.magic.core.CastingPose;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
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
    private record Motion(long since, boolean releasing, float initialWeight) {
        float weight(long tick, float partial) {
            float elapsed = tick - since + partial;
            return releasing ? CastingPose.lower(initialWeight, elapsed) : CastingPose.lift(elapsed);
        }
    }

    private static final Map<Integer, Motion> CASTERS = new HashMap<>();
    private static ClientLevel level;
    private static boolean held;

    private ClientChant() {}

    private static void useLevel(ClientLevel current) {
        if (level != current) {
            level = current;
            CASTERS.clear();
            while (KEY.consumeClick()) {
                // Discard presses queued in the previous world.
            }
            held = false;
        }
    }

    @SubscribeEvent
    public static void receive(ChantPackets.StateReceived event) {
        var state = event.state;
        useLevel(Minecraft.getInstance().level);
        if (level == null) return;
        if (state.phase() == ChantPackets.Phase.CHANTING) {
            long start = level.getGameTime() - (state.totalTicks() - state.remainingTicks());
            CASTERS.compute(state.casterId(), (id, old) -> new Motion(
                    old == null || old.releasing() ? start : Math.min(old.since(), start), false, 0));
        } else {
            long now = level.getGameTime();
            CASTERS.computeIfPresent(state.casterId(), (id, old) -> old.releasing() ? old
                    : new Motion(now, true, old.weight(now, 0)));
        }
    }

    public static float weight(Player player, float partial) {
        if (player.level() != level || !player.isAlive()) return 0;
        var motion = CASTERS.get(player.getId());
        return motion == null ? 0 : motion.weight(level.getGameTime(), partial);
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
        while (KEY.consumeClick()) {
            if (down && !held && mc.player.isAlive()) {
                PacketDistributor.sendToServer(new ChantPackets.Start());
                held = true;
            }
        }
        if (held && (!down || !mc.player.isAlive())) {
            PacketDistributor.sendToServer(new ChantPackets.Interrupt());
            held = false;
        }
        if (mc.isPaused()) return;
        CASTERS.entrySet().removeIf(entry -> {
            if (!(level.getEntity(entry.getKey()) instanceof Player caster) || !caster.isAlive()) return true;
            var motion = entry.getValue();
            float weight = motion.weight(level.getGameTime(), 0);
            if (motion.releasing()) return weight <= 0;
            var hand = CastAnchor.hand(caster, 1, weight);
            if (level.tickRateManager().runsNormally()) {
                level.addParticle(ParticleTypes.SPLASH, hand.x, hand.y, hand.z, 0, 0, 0);
            }
            return false;
        });
    }
}
