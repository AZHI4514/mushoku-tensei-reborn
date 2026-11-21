package com.azhi.mushoku.magic.runtime;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.core.Mana;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PlayerMana {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Mushoku.MOD_ID);
    public static final Supplier<AttachmentType<Integer>> MANA = ATTACHMENTS.register("mana",
            () -> AttachmentType.builder(() -> Mana.MAX)
                    .serialize(Codec.INT.xmap(Mana::clamp, value -> value))
                    .sync((holder, recipient) -> holder == recipient, ByteBufCodecs.VAR_INT)
                    .build());

    private PlayerMana() {}

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(MANA);
        }
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(MANA);
        }
    }

    static int set(ServerPlayer player, int value) {
        int current = Mana.clamp(value);
        player.setData(MANA, current);
        return current;
    }
}
