package org.a1kari8.mc.lastbreath.event;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.a1kari8.mc.lastbreath.ServerConfig;
import org.a1kari8.mc.lastbreath.ServerRescueManager;
import org.a1kari8.mc.lastbreath.network.RescueState;
import org.a1kari8.mc.lastbreath.network.payload.DyingStatePayload;
import org.a1kari8.mc.lastbreath.network.payload.RescueStatePayload;
import org.jetbrains.annotations.ApiStatus;

import static org.a1kari8.mc.lastbreath.LastBreath.MODID;

@EventBusSubscriber(modid = MODID)
public class DeathEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // 如果已经濒死，就允许死亡
        if (player.getPersistentData().getBoolean("Dying")) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (ServerRescueManager.isBeingRescued(player)) {
                    PacketDistributor.sendToPlayer(serverPlayer, new RescueStatePayload(RescueState.CANCEL));
                    PacketDistributor.sendToPlayer(ServerRescueManager.getRescuer(serverPlayer), new RescueStatePayload(RescueState.CANCEL));
                    ServerRescueManager.cancelBeingRescued(player);
                }
                PacketDistributor.sendToPlayer(serverPlayer, new DyingStatePayload(false));
            }
            AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed != null) {
                movementSpeed.setBaseValue(0.1); // 默认是 0.1
            }
            player.setForcedPose(null);
            player.getPersistentData().putBoolean("Dying", false);
            player.getPersistentData().putBoolean("Bleeding", false);
            return;
        }

        // 阻止死亡
        event.setCanceled(true);

        // 设置濒死状态
        setDying(player, (float) ServerConfig.DYING_HEALTH.getAsDouble());
//        player.addEffect(new MobEffectInstance(LastBreath.DYING_MOB_EFFECT, Integer.MAX_VALUE, 0, false, false));
    }

    @ApiStatus.Internal
    public static void setDying(Player player, float dyingHealth) {
        // 设置濒死状态
        player.getPersistentData().putBoolean("Dying", true);
        player.getPersistentData().putBoolean("Bleeding", true);
        player.setForcedPose(Pose.SWIMMING);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new DyingStatePayload(true));
        }
        player.setHealth(dyingHealth);
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(0.04); // 默认是 0.1
        }
    }
}
