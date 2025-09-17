package org.a1kari8.mc.lastbreath.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.a1kari8.mc.lastbreath.ServerConfig;
import org.a1kari8.mc.lastbreath.ServerRescueManager;
import org.a1kari8.mc.lastbreath.network.RescueState;
import org.a1kari8.mc.lastbreath.network.payload.DyingStatePayload;
import org.a1kari8.mc.lastbreath.network.payload.RescueStatePayload;
import org.jetbrains.annotations.ApiStatus;

import static org.a1kari8.mc.lastbreath.LastBreath.MODID;

@EventBusSubscriber(modid = MODID)
public class RescueEventHandler {
    @SubscribeEvent
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
            return;
        }
        if (!isRescuing) {
            handleStartRescue(serverRescuer, serverTarget);
            return;
        }
        handleUpdateRescue(serverRescuer, serverTarget);
        handleCompleteRescue(serverRescuer, serverTarget, event);
    }

    private static boolean canRescueAttempt(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player target)) return false;
        Player rescuer = event.getEntity();
        if (event.getLevel().isClientSide) return false;
        if (!target.getPersistentData().getBoolean("Dying") || rescuer.getPersistentData().getBoolean("Dying")) {
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
            rescuePlayer(serverTarget, (float) ServerConfig.RESCUE_HEALTH.getAsDouble());
            event.setCanceled(true);
        }
    }

    @ApiStatus.Internal
    public static void rescuePlayer(ServerPlayer serverTarget, float healthAfterRescue) {
        // 移除濒死状态
//                target.removeEffect(LastBreath.DYING_MOB_EFFECT);
        AttributeInstance movementSpeed = serverTarget.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(0.1); // 默认是 0.1
        }
        AttributeInstance maxHealth = serverTarget.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0); // 默认是 20.0
        }
        serverTarget.setHealth(healthAfterRescue);
        serverTarget.getPersistentData().putBoolean("Dying", false);
        serverTarget.getPersistentData().putBoolean("Bleeding", false);
        serverTarget.setForcedPose(null);
        PacketDistributor.sendToPlayer(serverTarget, new DyingStatePayload(false));
        serverTarget.level().playSound(null, serverTarget.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
//                rescuer.sendSystemMessage(Component.literal("你成功救援了 " + target.getName().getString()));
//                target.sendSystemMessage(Component.literal("你被 " + rescuer.getName().getString() + " 救援了！"));
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
            handleBleeding(serverPlayer);
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
        if (serverPlayer.getPersistentData().getBoolean("Dying") && serverPlayer.getPersistentData().getBoolean("Bleeding") && ServerConfig.BLEEDING_DURATION.getAsInt() > 0 && !ServerRescueManager.isBeingRescued(serverPlayer)) {
            if (serverPlayer.tickCount % 100 == 0) {
                float currentHealth = serverPlayer.getHealth();
                serverPlayer.setHealth(currentHealth - currentHealth * 5 / ServerConfig.BLEEDING_DURATION.getAsInt());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerExit(PlayerEvent.PlayerLoggedOutEvent event) {
        clearRescue(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        clearRescue(player);
        if (player.getPersistentData().getBoolean("Dying")) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, new DyingStatePayload(true));
        }
    }

    /**
     * 清除玩家的救援状态
     *
     * @param player
     */
    private static void clearRescue(Player player) {
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
