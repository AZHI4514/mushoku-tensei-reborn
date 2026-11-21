package com.azhi.mushoku.magic.runtime;

import com.azhi.mushoku.magic.core.Chant;
import com.azhi.mushoku.magic.core.Mana;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import static com.azhi.mushoku.magic.runtime.ChantPackets.Phase;

public final class PlayerChants {
    private static final Map<UUID, PlayerChants> ACTIVE = new HashMap<>();
    private final ServerPlayer owner;
    private final Chant chant = new Chant();
    private final Set<UUID> viewers = new HashSet<>();

    private PlayerChants(ServerPlayer owner) {
        this.owner = owner;
    }

    static void start(ServerPlayer player) {
        if (ACTIVE.containsKey(player.getUUID())) return;
        var session = new PlayerChants(player);
        var result = session.chant.start(player.getData(PlayerMana.MANA), player.isSpectator(), player.isAlive());
        if (result == Chant.Result.INSUFFICIENT_MANA) insufficientMana(player);
        if (result != Chant.Result.STARTED) return;
        ACTIVE.put(player.getUUID(), session);
        session.broadcast();
    }

    private static void insufficientMana(ServerPlayer player) {
        player.displayClientMessage(Component.literal("魔力不足"), true);
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var session = ACTIVE.get(player.getUUID());
        if (session == null) return;
        if (session.owner != player) {
            session.finish(Phase.INTERRUPTED);
            return;
        }
        if (!player.isAlive() || player.isSpectator()) {
            interrupt(player);
            return;
        }
        if (!player.serverLevel().tickRateManager().runsNormally()) return;
        int mana = player.getData(PlayerMana.MANA);
        switch (session.chant.tick(mana)) {
            case COMPLETED -> {
                PlayerMana.set(player, Mana.spend(mana, Chant.COST));
                session.finish(Phase.COMPLETED);
            }
            case INSUFFICIENT_MANA -> {
                insufficientMana(player);
                session.finish(Phase.INTERRUPTED);
            }
            default -> {
                if (session.chant.remainingTicks() % 20 == 0) session.broadcast();
            }
        }
    }

    private ChantPackets.State state(Phase phase) {
        return new ChantPackets.State(owner.getId(), phase, Chant.DURATION_TICKS, chant.remainingTicks());
    }

    private void broadcast() {
        var snapshot = state(Phase.CHANTING);
        var nearby = new HashSet<UUID>();
        for (var viewer : owner.serverLevel().players()) {
            if (viewer.distanceToSqr(owner) <= 48 * 48) {
                PacketDistributor.sendToPlayer(viewer, snapshot);
                nearby.add(viewer.getUUID());
            }
        }
        viewers.removeAll(nearby);
        notifyViewers(Phase.INTERRUPTED);
        viewers.addAll(nearby);
    }

    private void notifyViewers(Phase phase) {
        var snapshot = state(phase);
        for (UUID id : viewers) {
            var viewer = owner.serverLevel().getServer().getPlayerList().getPlayer(id);
            if (viewer != null) PacketDistributor.sendToPlayer(viewer, snapshot);
        }
        viewers.clear();
    }

    private void finish(Phase phase) {
        ACTIVE.remove(owner.getUUID(), this);
        notifyViewers(phase);
    }

    static void interrupt(ServerPlayer player) {
        var session = ACTIVE.get(player.getUUID());
        if (session != null) {
            session.chant.interrupt();
            session.finish(Phase.INTERRUPTED);
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
