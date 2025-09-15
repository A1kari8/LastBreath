package org.a1kari8.mc.lastbreath.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.jetbrains.annotations.NotNull;

public record RescueProgressPayload(
                                    RescueState rescueState) implements CustomPacketPayload {
    public static final Type<RescueProgressPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LastBreath.MODID, "rescue_progress"));

//    public static final StreamCodec<ByteBuf, UUID> UUID_CODEC = new StreamCodec<>() {
//        @Override
//        public @NotNull UUID decode(@NotNull ByteBuf buf) {
//            return new UUID(buf.readLong(), buf.readLong());
//        }
//
//        @Override
//        public void encode(@NotNull ByteBuf buf, @NotNull UUID uuid) {
//            buf.writeLong(uuid.getMostSignificantBits());
//            buf.writeLong(uuid.getLeastSignificantBits());
//        }
//    };

    public static final StreamCodec<ByteBuf, RescueState> STATE_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull RescueState decode(@NotNull ByteBuf buf) {
            return RescueState.values()[buf.readByte()];
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull RescueState rescueState) {
            buf.writeByte(rescueState.ordinal());
        }
    };

    public static final StreamCodec<ByteBuf, RescueProgressPayload> CODEC =
            StreamCodec.composite(
//                    UUID_CODEC, RescueProgressPayload::playerId,
//                    ByteBufCodecs.FLOAT, RescueProgressPayload::progress,
                    STATE_CODEC, RescueProgressPayload::rescueState,
                    RescueProgressPayload::new
            );


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}