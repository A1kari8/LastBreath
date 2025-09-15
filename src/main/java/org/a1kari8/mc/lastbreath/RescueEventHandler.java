package org.a1kari8.mc.lastbreath;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.a1kari8.mc.lastbreath.network.RescueState;

import static org.a1kari8.mc.lastbreath.LastBreath.MODID;

@EventBusSubscriber(modid = MODID)
public class RescueEventHandler {
    @SubscribeEvent
    public static void onRescueAttempt(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player target)) return;
        Player rescuer = event.getEntity();
        if (event.getLevel().isClientSide) return;

        if (!target.hasEffect(LastBreath.DYING_MOB_EFFECT)) return;
        if (target instanceof ServerPlayer serverTarget && rescuer instanceof ServerPlayer serverRescuer) {
            boolean isRescuing = RescueManager.isBeingRescued(target) || RescueManager.isRescuing(rescuer);
            if (!rescuer.isCrouching()) {
                if (isRescuing) {
                    PacketDistributor.sendToPlayer(serverTarget, new RescueProgressPayload(RescueState.CANCELLED));
                    PacketDistributor.sendToPlayer(serverRescuer, new RescueProgressPayload(RescueState.CANCELLED));
                    RescueManager.cancelRescuing(rescuer);
                }
                return;
            }
            if (!isRescuing) {
                PacketDistributor.sendToPlayer(serverTarget, new RescueProgressPayload( RescueState.RESCUING));
                PacketDistributor.sendToPlayer(serverRescuer, new RescueProgressPayload( RescueState.RESCUING));
            }
            RescueManager.updateRescue(rescuer, target);
            if (RescueManager.isBeingRescuedComplete(target) || RescueManager.isRescuingComplete(rescuer)) {
                PacketDistributor.sendToPlayer(serverTarget, new RescueProgressPayload(RescueState.COMPLETED));
                PacketDistributor.sendToPlayer(serverRescuer, new RescueProgressPayload( RescueState.COMPLETED));
                target.removeEffect(LastBreath.DYING_MOB_EFFECT);
                target.setHealth(6.0F);
                target.getPersistentData().putBoolean("Dying", false);
                target.setForcedPose(null);
                event.getLevel().playSound(null, target.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                rescuer.sendSystemMessage(Component.literal("你成功救援了 " + target.getName().getString()));
                target.sendSystemMessage(Component.literal("你被 " + rescuer.getName().getString() + " 救援了！"));
                event.setCanceled(true);
            }
        }
    }

//    @SubscribeEvent
//    public static void onPlayerTick(PlayerTickEvent.Post event) {
//        Player player = event.getEntity();
//
//        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
//            // 正在救援中
//            if (RescueManager.isRescuing(player)) {
//                float progress = RescueManager.getRescuingProgress(player);
//                if (RescueManager.shouldSyncProgress(player, progress)) {
//                    PacketDistributor.sendToPlayer(RescueManager.getTarget(serverPlayer), new RescueProgressPayload(progress, RescueState.RESCUING));
//                    PacketDistributor.sendToPlayer(serverPlayer, new RescueProgressPayload(progress, RescueState.RESCUING));
//                }
//                // 玩家取消救援（松开蹲键）
//                if (!player.isCrouching()) {
//                    PacketDistributor.sendToPlayer(RescueManager.getTarget(serverPlayer), new RescueProgressPayload(0.0f, RescueState.CANCELLED));
//                    PacketDistributor.sendToPlayer(serverPlayer, new RescueProgressPayload(0.0f, RescueState.CANCELLED));
//                    RescueManager.cancelRescuing(player);
//                }
//                // 正在被救援
//            } else if (RescueManager.isBeingRescued(player)) {
//                float progress = RescueManager.getBeingRescuedProgress(player);
//                if (RescueManager.shouldSyncProgress(player, progress)) {
//                    PacketDistributor.sendToPlayer(RescueManager.getRescuer(serverPlayer), new RescueProgressPayload(progress, RescueState.RESCUING));
//                    PacketDistributor.sendToPlayer(serverPlayer, new RescueProgressPayload(progress, RescueState.RESCUING));
//                }
//            } else if (RescueManager.isBeingRescuedComplete(player)) {
//                PacketDistributor.sendToPlayer(RescueManager.getRescuer(serverPlayer), new RescueProgressPayload(1.0f, RescueState.COMPLETED));
//                PacketDistributor.sendToPlayer(serverPlayer, new RescueProgressPayload(1.0f, RescueState.COMPLETED));
//            } else if (RescueManager.isRescuingComplete(player)) {
//                PacketDistributor.sendToPlayer(RescueManager.getTarget(serverPlayer), new RescueProgressPayload(1.0f, RescueState.COMPLETED));
//                PacketDistributor.sendToPlayer(serverPlayer, new RescueProgressPayload(1.0f, RescueState.COMPLETED));
//            }
//        }
//    }
}
