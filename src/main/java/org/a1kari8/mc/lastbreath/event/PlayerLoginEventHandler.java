package org.a1kari8.mc.lastbreath.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.ServerDyingManager;
import org.a1kari8.mc.lastbreath.network.payload.DyingListPayload;
import org.a1kari8.mc.lastbreath.network.payload.DyingStatePayload;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = LastBreath.MOD_ID)
public class PlayerLoginEventHandler {

    @SubscribeEvent
    public static void onPlayerExit(PlayerEvent.PlayerLoggedOutEvent event) {
        RescueEventHandler.clearRescue(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        RescueEventHandler.clearRescue(player);
        if (player.getData(LastBreath.DYING)) {
            ServerDyingManager.addDying(player.getUUID());
            PacketDistributor.sendToPlayer((ServerPlayer) player, new DyingStatePayload(true));
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            List<UUID> dyingList = server.getPlayerList().getPlayers().stream()
                    .filter(serverPlayer -> serverPlayer.getData(LastBreath.DYING) && !serverPlayer.getUUID().equals(player.getUUID()))
                    .map(Entity::getUUID)
                    .toList();
            if (!dyingList.isEmpty()){
                PacketDistributor.sendToPlayer((ServerPlayer) player, new DyingListPayload(dyingList));
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.getData(LastBreath.DYING)) {
                ServerDyingManager.addDying(player.getUUID());
            }
        }
    }
}
