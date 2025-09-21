package org.a1kari8.mc.lastbreath.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.network.RescueState;
import org.jetbrains.annotations.NotNull;

public record RescueStatePayload(RescueState rescueState) implements CustomPacketPayload {
    public static final Type<RescueStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LastBreath.MOD_ID, "rescue_progress"));

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

    public static final StreamCodec<FriendlyByteBuf, RescueState> STATE_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull RescueState decode(@NotNull FriendlyByteBuf buf) {
            return buf.readEnum(RescueState.class);
        }

        @Override
        public void encode(@NotNull FriendlyByteBuf buf, @NotNull RescueState rescueState) {
            buf.writeEnum(rescueState);
        }
    };

    public static final StreamCodec<FriendlyByteBuf, RescueStatePayload> CODEC =
            StreamCodec.composite(
//                    UUID_CODEC, RescueProgressPayload::playerId,
//                    ByteBufCodecs.FLOAT, RescueProgressPayload::progress,
                    STATE_CODEC, RescueStatePayload::rescueState, RescueStatePayload::new
            );


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}