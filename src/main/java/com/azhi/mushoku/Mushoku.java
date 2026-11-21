package com.azhi.mushoku;

import com.azhi.mushoku.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Mushoku.MOD_ID)
public final class Mushoku {
    public static final String MOD_ID = "mushoku_tensei_reborn";

    public Mushoku(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
    }
}
