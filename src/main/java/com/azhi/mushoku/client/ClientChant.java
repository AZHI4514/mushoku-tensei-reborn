package com.azhi.mushoku.client;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.runtime.CastAnchor;
import com.azhi.mushoku.magic.runtime.ChantPackets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
    private static final Set<Integer> CASTERS = new HashSet<>();
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
            CASTERS.add(state.casterId());
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
        var staleIds = new ArrayList<Integer>();
        CASTERS.forEach(id -> {
            if (!(level.getEntity(id) instanceof Player caster)) {
                staleIds.add(id);
                return;
            }
            var hand = CastAnchor.hand(caster);
            level.addParticle(ParticleTypes.SPLASH, hand.x, hand.y, hand.z, 0, 0, 0);
        });
        CASTERS.removeAll(staleIds);
    }
}
