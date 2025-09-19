package org.a1kari8.mc.lastbreath.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.ServerConfig;
import org.a1kari8.mc.lastbreath.ServerRescueManager;
import org.a1kari8.mc.lastbreath.client.ClientRescueManager;
import org.a1kari8.mc.lastbreath.network.RescueState;
import org.a1kari8.mc.lastbreath.network.payload.RescueStatePayload;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import static org.a1kari8.mc.lastbreath.LastBreath.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class RescueEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRescueAttempt(PlayerInteractEvent.EntityInteract event) {
        if (!canRescueAttempt(event)) return;
        Player target = (Player) event.getTarget();
        Player rescuer = event.getEntity();

        boolean isRescuing = ServerRescueManager.isBeingRescued(target) || ServerRescueManager.isRescuing(rescuer);

        if (!(target instanceof ServerPlayer serverTarget) || !(rescuer instanceof ServerPlayer serverRescuer)) return;
        if (!rescuer.isCrouching()) {
            if (isRescuing) {
                handleCancelRescue(serverTarget, serverRescuer);
            }
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        if (!isRescuing) {
            handleStartRescue(serverRescuer, serverTarget);
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }
        handleUpdateRescue(serverRescuer, serverTarget);
        handleCompleteRescue(serverRescuer, serverTarget, event);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (ClientRescueManager.getState() == RescueState.RESCUING) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    private static boolean canRescueAttempt(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player target)) return false;
        Player rescuer = event.getEntity();
        if (event.getLevel().isClientSide) {
            return false;
        }
        if (!target.getData(LastBreath.DYING) || rescuer.getData(LastBreath.DYING)) {
            return false;
        }
        return target instanceof ServerPlayer && rescuer instanceof ServerPlayer;
    }

    private static void handleCancelRescue(ServerPlayer serverTarget, ServerPlayer serverRescuer) {
        ServerRescueManager.cancelRescuing(serverRescuer);
        PacketDistributor.sendToPlayer(serverTarget, new RescueStatePayload(RescueState.CANCEL));
        PacketDistributor.sendToPlayer(serverRescuer, new RescueStatePayload(RescueState.CANCEL));
    }

    private static void handleStartRescue(ServerPlayer serverRescuer, ServerPlayer serverTarget) {
        ServerRescueManager.startRescue(serverRescuer, serverTarget);
        PacketDistributor.sendToPlayer(serverTarget, new RescueStatePayload(RescueState.START));
        PacketDistributor.sendToPlayer(serverRescuer, new RescueStatePayload(RescueState.START));
    }

    private static void handleUpdateRescue(ServerPlayer serverRescuer, ServerPlayer serverTarget) {
        ServerRescueManager.updateRescue(serverRescuer, serverTarget);
    }

    private static void handleCompleteRescue(ServerPlayer serverRescuer, ServerPlayer serverTarget, PlayerInteractEvent.EntityInteract event) {
        if (ServerRescueManager.isBeingRescuedComplete(serverTarget) || ServerRescueManager.isRescuingComplete(serverRescuer)) {
            ServerRescueManager.completeBeingRescued(serverTarget);
            PacketDistributor.sendToPlayer(serverTarget, new RescueStatePayload(RescueState.COMPLETE));
            PacketDistributor.sendToPlayer(serverRescuer, new RescueStatePayload(RescueState.COMPLETE));
            rescuePlayer(serverRescuer, serverTarget, (float) ServerConfig.RESCUE_HEALTH.getAsDouble());
        }
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    @ApiStatus.Internal
    public static void rescuePlayer(@Nullable ServerPlayer serverRescuer, ServerPlayer serverTarget, float healthAfterRescue) {
        DeathEventHandler.clearDyingState(serverTarget);
        serverTarget.setHealth(healthAfterRescue);
        serverTarget.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 254, false, false));
        serverTarget.level().playSound(null, serverTarget.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && serverRescuer != null) {
            server.getPlayerList().broadcastSystemMessage(Component.literal(serverRescuer.getName().getString() + "救了" + serverTarget.getName().getString()),false);
        }
    }


    /**
     * 判断施救玩家是否松开右键
     *
     * @param event
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            handleRescueCancel(serverPlayer);
            if (player.getData(LastBreath.DYING)) {
                handleBleeding(serverPlayer);
            }
        }
    }

    /**
     * 处理救援取消逻辑
     */
    private static void handleRescueCancel(ServerPlayer serverPlayer) {
        if (ServerRescueManager.isRescuing(serverPlayer)) {
            if (ServerRescueManager.isRightClickReleased(serverPlayer)) {
                PacketDistributor.sendToPlayer(ServerRescueManager.getTarget(serverPlayer), new RescueStatePayload(RescueState.CANCEL));
                PacketDistributor.sendToPlayer(serverPlayer, new RescueStatePayload(RescueState.CANCEL));
                ServerRescueManager.cancelRescuing(serverPlayer);
            }
        }
    }

    /**
     * 处理流血逻辑
     */
    private static void handleBleeding(ServerPlayer serverPlayer) {
        if (serverPlayer.getData(LastBreath.BLEEDING) && ServerConfig.BLEEDING_DURATION.getAsInt() > 0 && !ServerRescueManager.isBeingRescued(serverPlayer)) {
            if (serverPlayer.tickCount % 100 == 0) {
                float currentHealth = serverPlayer.getHealth();
                serverPlayer.setHealth(currentHealth - currentHealth * 5 / ServerConfig.BLEEDING_DURATION.getAsInt());
            }
        }
    }

    /**
     * 清除玩家的救援状态
     *
     * @param player
     */
    @ApiStatus.Internal
    public static void clearRescue(Player player) {
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (ServerRescueManager.isBeingRescued(player)) {
            PacketDistributor.sendToPlayer(serverPlayer, new RescueStatePayload(RescueState.CANCEL));
            PacketDistributor.sendToPlayer(ServerRescueManager.getRescuer(serverPlayer), new RescueStatePayload(RescueState.CANCEL));
            ServerRescueManager.cancelRescuing(player);
        } else if (ServerRescueManager.isRescuing(player)) {
            PacketDistributor.sendToPlayer(serverPlayer, new RescueStatePayload(RescueState.CANCEL));
            PacketDistributor.sendToPlayer(ServerRescueManager.getTarget(serverPlayer), new RescueStatePayload(RescueState.CANCEL));
            ServerRescueManager.cancelRescuing(player);
        }
    }
}
