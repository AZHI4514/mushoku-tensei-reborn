package com.azhi.mushoku.magic.runtime;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.core.Chant;
import com.azhi.mushoku.magic.core.Mana;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import static com.azhi.mushoku.magic.runtime.ChantPackets.Phase;

public final class PlayerChants {
    private static final AttributeModifier SLOW = new AttributeModifier(
            ResourceLocation.fromNamespaceAndPath(Mushoku.MOD_ID, "chant_slowdown"),
            -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final Map<ServerPlayer, PlayerChants> ACTIVE = new HashMap<>();
    private final Chant chant = new Chant();
    private final Set<UUID> viewers = new HashSet<>();

    private PlayerChants() {}

    static void start(ServerPlayer player) {
        if (ACTIVE.containsKey(player)) return;
        var session = new PlayerChants();
        var result = session.chant.start(player.getData(PlayerMana.MANA), player.isSpectator(), player.isAlive());
        if (result == Chant.Result.INSUFFICIENT_MANA) insufficientMana(player);
        if (result != Chant.Result.STARTED) return;
        ACTIVE.put(player, session);
        player.getAttribute(Attributes.MOVEMENT_SPEED).addOrUpdateTransientModifier(SLOW);
        session.broadcast(player);
    }

    private static void insufficientMana(ServerPlayer player) {
        player.displayClientMessage(Component.literal("魔力不足"), true);
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var session = ACTIVE.get(player);
        if (session == null) return;
        if (!player.isAlive() || player.isSpectator()) {
            interrupt(player);
            return;
        }
        if (!player.serverLevel().tickRateManager().runsNormally()) return;
        int mana = player.getData(PlayerMana.MANA);
        switch (session.chant.tick(mana)) {
            case COMPLETED -> {
                PlayerMana.set(player, Mana.spend(mana, Chant.COST));
                session.finish(player, Phase.COMPLETED);
            }
            case INSUFFICIENT_MANA -> {
                insufficientMana(player);
                session.finish(player, Phase.INTERRUPTED);
            }
            default -> {
                if (session.chant.remainingTicks() % 20 == 0) session.broadcast(player);
            }
        }
    }

    private ChantPackets.State state(ServerPlayer player, Phase phase) {
        return new ChantPackets.State(player.getId(), phase, Chant.DURATION_TICKS, chant.remainingTicks());
    }

    private void broadcast(ServerPlayer player) {
        var snapshot = state(player, Phase.CHANTING);
        var nearby = new HashSet<UUID>();
        for (var viewer : player.serverLevel().players()) {
            if (viewer.distanceToSqr(player) <= 48 * 48) {
                PacketDistributor.sendToPlayer(viewer, snapshot);
                nearby.add(viewer.getUUID());
            }
        }
        viewers.removeAll(nearby);
        notifyViewers(player, Phase.INTERRUPTED);
        viewers.addAll(nearby);
    }

    private void notifyViewers(ServerPlayer player, Phase phase) {
        var snapshot = state(player, phase);
        for (UUID id : viewers) {
            var viewer = player.serverLevel().getServer().getPlayerList().getPlayer(id);
            if (viewer != null) PacketDistributor.sendToPlayer(viewer, snapshot);
        }
        viewers.clear();
    }

    private void finish(ServerPlayer player, Phase phase) {
        ACTIVE.remove(player);
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SLOW.id());
        notifyViewers(player, phase);
    }

    static void interrupt(ServerPlayer player) {
        var session = ACTIVE.get(player);
        if (session != null) {
            session.chant.interrupt();
            session.finish(player, Phase.INTERRUPTED);
        }
    }

    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) interrupt(player);
    }

    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) interrupt(player);
    }

    public static void dimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) interrupt(player);
    }
}
