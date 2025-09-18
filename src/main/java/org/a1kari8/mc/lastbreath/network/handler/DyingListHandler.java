package org.a1kari8.mc.lastbreath.network.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.network.payload.DyingListPayload;

import java.util.UUID;

public class DyingListHandler {
    public static void handleDataOnMain(final DyingListPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                for (UUID uuid : data.dyingList()) {
                    Player player = level.getPlayerByUUID(uuid);
                    if (player != null) {
                        player.setForcedPose(Pose.SWIMMING);
                        player.setData(LastBreath.DYING, true);
                    }
                }
            }
        });
    }
}
