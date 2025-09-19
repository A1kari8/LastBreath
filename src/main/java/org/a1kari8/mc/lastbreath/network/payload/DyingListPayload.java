package org.a1kari8.mc.lastbreath.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record DyingListPayload(List<UUID> dyingList) implements CustomPacketPayload {
    public static final Type<DyingListPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LastBreath.MOD_ID, "dying_list"));

        public static final StreamCodec<FriendlyByteBuf, List<UUID>> DYING_LIST_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull List<UUID> decode(@NotNull FriendlyByteBuf buf) {
            int size = buf.readShort();
            List<UUID> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(buf.readUUID());
            }
            return list;
        }

        @Override
        public void encode(@NotNull FriendlyByteBuf buf, @NotNull List<UUID> dyingList) {
            buf.writeShort(dyingList.size());
            for (UUID uuid : dyingList) {
                buf.writeUUID(uuid);
            }
        }
    };

    public static final StreamCodec<FriendlyByteBuf, DyingListPayload> CODEC =
            StreamCodec.composite(
                    DYING_LIST_CODEC, DyingListPayload::dyingList,
                    DyingListPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
