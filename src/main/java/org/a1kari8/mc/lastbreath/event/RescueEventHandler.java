package org.a1kari8.mc.lastbreath.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.ServerRescueManager;
import org.a1kari8.mc.lastbreath.network.RescueProgressPayload;
import org.a1kari8.mc.lastbreath.network.RescueState;

import static org.a1kari8.mc.lastbreath.LastBreath.MODID;

@EventBusSubscriber(modid = MODID)
public class RescueEventHandler {
    @SubscribeEvent
    public static void onRescueAttempt(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player target)) return;
        Player rescuer = event.getEntity();
        if (event.getLevel().isClientSide) return;

        if (!target.hasEffect(LastBreath.DYING_MOB_EFFECT)) {
            // 被救援者没有濒死状态，无法救援
            return;
        }

        if (target instanceof ServerPlayer serverTarget && rescuer instanceof ServerPlayer serverRescuer) {
            boolean isRescuing = ServerRescueManager.isBeingRescued(target) || ServerRescueManager.isRescuing(rescuer);

            if (!rescuer.isCrouching()) {
                // 如果施救者没有蹲下
                if (isRescuing) {
                    // 如果当前正在救援中，则取消救援
                    ServerRescueManager.cancelRescuing(rescuer);
                    // 向客户端发送取消救援的消息
                    PacketDistributor.sendToPlayer(serverTarget, new RescueProgressPayload(RescueState.CANCEL));
                    PacketDistributor.sendToPlayer(serverRescuer, new RescueProgressPayload(RescueState.CANCEL));
                }
                // 否则什么也不做
                return;
            }
            if (!isRescuing) {
                // 开始救援
                ServerRescueManager.startRescue(rescuer,target);
                // 向客户端发送开始救援的消息
                PacketDistributor.sendToPlayer(serverTarget, new RescueProgressPayload( RescueState.START));
                PacketDistributor.sendToPlayer(serverRescuer, new RescueProgressPayload( RescueState.START));
                return;
            }

            if (ServerRescueManager.isRightClickReleased(target)) {
                // 判断右键松开则取消救援
                ServerRescueManager.cancelRescuing(rescuer);
                // 向客户端发送取消救援的消息
                PacketDistributor.sendToPlayer(serverTarget, new RescueProgressPayload( RescueState.CANCEL));
                PacketDistributor.sendToPlayer(serverRescuer, new RescueProgressPayload( RescueState.CANCEL));
                return;
            }

            // 更新救援进度
            ServerRescueManager.updateRescue(rescuer, target);
            if (ServerRescueManager.isBeingRescuedComplete(target) || ServerRescueManager.isRescuingComplete(rescuer)) {
                // 判断救援是否完成
                ServerRescueManager.completeBeingRescued(target);
                // 向客户端发送救援完成的消息
                PacketDistributor.sendToPlayer(serverTarget, new RescueProgressPayload(RescueState.COMPLETE));
                PacketDistributor.sendToPlayer(serverRescuer, new RescueProgressPayload( RescueState.COMPLETE));

                // 移除濒死状态
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
//                if (!player.isCrouching()) {
//                    PacketDistributor.sendToPlayer(RescueManager.getTarget(serverPlayer), new RescueProgressPayload( RescueState.CANCELLED));
//                    PacketDistributor.sendToPlayer(serverPlayer, new RescueProgressPayload(RescueState.CANCELLED));
//                    RescueManager.cancelRescuing(player);
//                }
//            }
//        }
//    }
}
