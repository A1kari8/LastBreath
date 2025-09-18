package org.a1kari8.mc.lastbreath.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.jetbrains.annotations.NotNull;

public record DyingStatePayload(boolean isDying) implements CustomPacketPayload {
    public static final Type<DyingStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LastBreath.MOD_ID, "dying_state"));
    public static final StreamCodec<ByteBuf, DyingStatePayload> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, DyingStatePayload::isDying, DyingStatePayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
