package com.azhi.mushoku.client;

import com.azhi.mushoku.magic.runtime.PlayerMana;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ClientMana {
    private ClientMana() {}

    public static Optional<Integer> current() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? Optional.empty() : player.getExistingData(PlayerMana.MANA);
    }
}
