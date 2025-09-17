package org.a1kari8.mc.lastbreath.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.a1kari8.mc.lastbreath.ServerConfig;
import org.a1kari8.mc.lastbreath.ServerRescueManager;
import org.a1kari8.mc.lastbreath.network.RescueState;
import org.a1kari8.mc.lastbreath.network.payload.DyingStatePayload;
import org.a1kari8.mc.lastbreath.network.payload.RescueStatePayload;

import static org.a1kari8.mc.lastbreath.LastBreath.MODID;

@EventBusSubscriber(modid = MODID)
public class RescueEventHandler {
    @SubscribeEvent
    public static void onRescueAttempt(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player target)) return;
        Player rescuer = event.getEntity();
        if (event.getLevel().isClientSide) return;

        if (!target.getPersistentData().getBoolean("Dying") || rescuer.getPersistentData().getBoolean("Dying")){
            // 被救援者没有濒死状态或救援者是濒死状态，无法救援
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
                    PacketDistributor.sendToPlayer(serverTarget, new RescueStatePayload(RescueState.CANCEL));
                    PacketDistributor.sendToPlayer(serverRescuer, new RescueStatePayload(RescueState.CANCEL));
                }
                // 否则什么也不做
                return;
            }
            if (!isRescuing) {
                // 开始救援
                ServerRescueManager.startRescue(rescuer,target);
                // 向客户端发送开始救援的消息
                PacketDistributor.sendToPlayer(serverTarget, new RescueStatePayload( RescueState.START));
                PacketDistributor.sendToPlayer(serverRescuer, new RescueStatePayload( RescueState.START));
                return;
            }

            // 更新救援进度
            ServerRescueManager.updateRescue(rescuer, target);
            if (ServerRescueManager.isBeingRescuedComplete(target) || ServerRescueManager.isRescuingComplete(rescuer)) {
                // 判断救援是否完成
                ServerRescueManager.completeBeingRescued(target);
                // 向客户端发送救援完成的消息
                PacketDistributor.sendToPlayer(serverTarget, new RescueStatePayload(RescueState.COMPLETE));
                PacketDistributor.sendToPlayer(serverRescuer, new RescueStatePayload( RescueState.COMPLETE));

                rescuePlayer(serverTarget, (float) ServerConfig.RESCUE_HEALTH.getAsDouble());
                event.setCanceled(true);
            }
        }
    }

    public static void rescuePlayer(ServerPlayer serverTarget, float healthAfterRescue) {
        // 移除濒死状态
//                target.removeEffect(LastBreath.DYING_MOB_EFFECT);
        AttributeInstance movementSpeed = serverTarget.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(0.1); // 默认是 0.1
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
     * @param event
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            // 正在救援中
            if (ServerRescueManager.isRescuing(player)) {
                if (ServerRescueManager.isRightClickReleased(player)) {
                    // 向客户端发送取消救援的消息
                    PacketDistributor.sendToPlayer(ServerRescueManager.getTarget(serverPlayer), new RescueStatePayload( RescueState.CANCEL));
                    PacketDistributor.sendToPlayer(serverPlayer, new RescueStatePayload( RescueState.CANCEL));
                    // 判断右键松开则取消救援
                    ServerRescueManager.cancelRescuing(serverPlayer);
                    return;
                }
            }
            if (player.getPersistentData().getBoolean("Dying") && player.getPersistentData().getBoolean("Bleeding") && ServerConfig.BLEEDING_DURATION.getAsInt() > 0 && !ServerRescueManager.isBeingRescued(player)) {
                // 如果濒死玩家没有被救援且开启流血配置项，则流血
                if (player.tickCount % 20 == 0) {
                    float currentHealth = player.getHealth();
                    player.setHealth(currentHealth - currentHealth / ServerConfig.BLEEDING_DURATION.getAsInt());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerExit(PlayerEvent.PlayerLoggedOutEvent event) {
        clearRescue(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        clearRescue(event.getEntity());
    }

    /**
     * 清除玩家的救援状态
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

    /**
     * 拦截倒地玩家的攻击行为
     * @param event
     */
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (player.getPersistentData().getBoolean("Dying")) {
            event.setCanceled(true); // 阻止攻击
        }
    }

    /**
     * 拦截倒地玩家使用远程武器
     * @param event
     */
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().getPersistentData().getBoolean("Dying")) {
            ItemStack stack = event.getItemStack();
            if (isRangedWeapon(stack)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ProjectileWeaponItem || item instanceof TridentItem;
    }

}
