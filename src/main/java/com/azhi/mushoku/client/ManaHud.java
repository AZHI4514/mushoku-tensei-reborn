package com.azhi.mushoku.client;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.core.Mana;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Mushoku.MOD_ID, value = Dist.CLIENT)
public final class ManaHud {
    private ManaHud() {}

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, ResourceLocation.fromNamespaceAndPath(Mushoku.MOD_ID, "mana"),
                (graphics, delta) -> ClientMana.current().ifPresent(value -> draw(graphics, value)));
    }

    private static void draw(GuiGraphics graphics, int value) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) return;
        int center = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() - Math.max(minecraft.gui.leftHeight, minecraft.gui.rightHeight) - 18;
        int left = center - 50;
        graphics.fill(left - 1, y - 1, left + 101, y + 4, 0xDC182338);
        graphics.fill(left, y, left + 100 * value / Mana.MAX, y + 3, 0xFF59AFFF);
        graphics.drawCenteredString(minecraft.font, value + " / " + Mana.MAX, center, y - 10, 0xFFFFFFFF);
    }
}
