package com.azhi.mushoku.magic.runtime;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.client.ClientChant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ChantPackets {
    private ChantPackets() {}

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> id(String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Mushoku.MOD_ID, path));
    }

    public record Start() implements CustomPacketPayload {
        public static final Type<Start> TYPE = id("start_chant");
        public Type<Start> type() { return TYPE; }
    }

    public record Interrupt() implements CustomPacketPayload {
        public static final Type<Interrupt> TYPE = id("interrupt_chant");
        public Type<Interrupt> type() { return TYPE; }
    }

    public enum Phase { CHANTING, COMPLETED, INTERRUPTED }

    public record State(int casterId, Phase phase, int totalTicks, int remainingTicks) implements CustomPacketPayload {
        public static final Type<State> TYPE = id("chant_state");
        private static final StreamCodec<RegistryFriendlyByteBuf, State> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, State::casterId,
                StreamCodec.of((buf, phase) -> buf.writeEnum(phase), buf -> buf.readEnum(Phase.class)), State::phase,
                ByteBufCodecs.VAR_INT, State::totalTicks,
                ByteBufCodecs.VAR_INT, State::remainingTicks, State::new);
        public Type<State> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(Start.TYPE, StreamCodec.unit(new Start()),
                (packet, ctx) -> PlayerChants.start((ServerPlayer) ctx.player()));
        registrar.playToServer(Interrupt.TYPE, StreamCodec.unit(new Interrupt()),
                (packet, ctx) -> PlayerChants.interrupt((ServerPlayer) ctx.player()));
        registrar.playToClient(State.TYPE, State.CODEC, (packet, ctx) -> ClientChant.receive(packet));
    }
}
