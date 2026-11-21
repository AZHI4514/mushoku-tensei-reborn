package com.azhi.mushoku.magic.runtime;

import com.azhi.mushoku.magic.core.Mana;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class ManaCommands {
    private ManaCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mushoku")
                .then(Commands.literal("mana").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            return report(context.getSource(), player, player.getData(PlayerMana.MANA));
                                        })))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("value", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    int value = IntegerArgumentType.getInteger(context, "value");
                                                    return report(context.getSource(), player, PlayerMana.set(player, value));
                                                }))))));
    }

    private static int report(CommandSourceStack source, ServerPlayer player, int value) {
        source.sendSuccess(() -> Component.literal(player.getScoreboardName() + ": " + value + " / " + Mana.MAX), false);
        return value;
    }
}
