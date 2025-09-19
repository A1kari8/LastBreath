package org.a1kari8.mc.lastbreath.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.ServerDyingManager;
import org.a1kari8.mc.lastbreath.network.payload.DyingListPayload;
import org.a1kari8.mc.lastbreath.network.payload.DyingStatePayload;

@EventBusSubscriber(modid = LastBreath.MOD_ID)
public class PlayerLoginEventHandler {

    @SubscribeEvent
    public static void onPlayerExit(PlayerEvent.PlayerLoggedOutEvent event) {
        RescueEventHandler.clearRescue(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        RescueEventHandler.clearRescue(player);
        if (player.getData(LastBreath.DYING)) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, new DyingStatePayload(true));
        }
        if (!ServerDyingManager.getDying().isEmpty()) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, new DyingListPayload(ServerDyingManager.getDying()));
        }
    }
}
