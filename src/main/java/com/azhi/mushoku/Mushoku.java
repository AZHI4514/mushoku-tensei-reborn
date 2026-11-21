package com.azhi.mushoku;

import com.azhi.mushoku.magic.runtime.ManaCommands;
import com.azhi.mushoku.magic.runtime.PlayerMana;
import com.azhi.mushoku.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Mushoku.MOD_ID)
public final class Mushoku {
    public static final String MOD_ID = "mushoku_tensei_reborn";

    public Mushoku(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
        PlayerMana.ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(PlayerMana::onLogin);
        NeoForge.EVENT_BUS.addListener(PlayerMana::onRespawn);
        NeoForge.EVENT_BUS.addListener(ManaCommands::register);
    }
}
