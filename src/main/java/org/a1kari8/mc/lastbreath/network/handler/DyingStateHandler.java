package org.a1kari8.mc.lastbreath.network.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.a1kari8.mc.lastbreath.network.payload.DyingStatePayload;

public class DyingStateHandler {
    public static void handleDataOnMain(final DyingStatePayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.setForcedPose(data.isDying() ? Pose.SWIMMING : null);
            }
        });
    }
}