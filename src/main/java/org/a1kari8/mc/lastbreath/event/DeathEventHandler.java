package org.a1kari8.mc.lastbreath.event;


import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.ServerConfig;
import org.a1kari8.mc.lastbreath.ServerDyingManager;
import org.a1kari8.mc.lastbreath.ServerRescueManager;
import org.a1kari8.mc.lastbreath.network.RescueState;
import org.a1kari8.mc.lastbreath.network.payload.DyingStatePayload;
import org.a1kari8.mc.lastbreath.network.payload.RescueStatePayload;
import org.jetbrains.annotations.ApiStatus;

import static org.a1kari8.mc.lastbreath.LastBreath.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class DeathEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // 如果已经濒死，就允许死亡
        if (player.getData(LastBreath.DYING)) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (ServerRescueManager.isBeingRescued(player)) {
                    PacketDistributor.sendToPlayer(serverPlayer, new RescueStatePayload(RescueState.CANCEL));
                    PacketDistributor.sendToPlayer(ServerRescueManager.getRescuer(serverPlayer), new RescueStatePayload(RescueState.CANCEL));
                    ServerRescueManager.cancelBeingRescued(player);
                }
            }
            return;
        }

        // 阻止死亡
        event.setCanceled(true);

        // 设置濒死状态
        setDying(player, (float) ServerConfig.DYING_HEALTH.getAsDouble());
//        player.addEffect(new MobEffectInstance(LastBreath.DYING_MOB_EFFECT, Integer.MAX_VALUE, 0, false, false));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.getData(LastBreath.DYING) && player instanceof ServerPlayer serverPlayer) {
            clearDyingState(serverPlayer);
            serverPlayer.setHealth(serverPlayer.getMaxHealth());
        }
    }

    @ApiStatus.Internal
    public static void clearDyingState(ServerPlayer serverPlayer) {
        AttributeInstance movementSpeed = serverPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(ResourceLocation.fromNamespaceAndPath(MOD_ID, "dying_speed_modifier"));
        }
        AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0); // 默认是 20.0
        }
        serverPlayer.setForcedPose(null);
        serverPlayer.setData(LastBreath.DYING, false);
        serverPlayer.setData(LastBreath.BLEEDING, false);
        serverPlayer.setInvulnerable(false);
        PacketDistributor.sendToPlayer(serverPlayer, new DyingStatePayload(false));
        ServerDyingManager.removeDying(serverPlayer.getUUID());
    }

    @ApiStatus.Internal
    public static void setDying(Player player, float dyingHealth) {
        // 设置濒死状态
        player.setData(LastBreath.DYING, true);
        player.setData(LastBreath.BLEEDING, true);
        player.setForcedPose(Pose.SWIMMING);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new DyingStatePayload(true));
        }
        player.setHealth(dyingHealth);
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(ResourceLocation.fromNamespaceAndPath(MOD_ID, "dying_speed_modifier"));
            movementSpeed.addOrReplacePermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(MOD_ID, "dying_speed_modifier"), ServerConfig.DYING_SPEED_MULTIPLE.getAsDouble() - 1.0f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(ServerConfig.DYING_MAX_HEALTH.getAsDouble()); // 默认是 20.0
        }
        ServerDyingManager.addDying(player.getUUID());
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage(Component.literal(player.getName().getString() + "倒地了"), false);
        }
        player.setInvulnerable(ServerConfig.DYING_INVULNERABLE.getAsBoolean());
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 254, false, false));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getData(LastBreath.DYING) && player.tickCount % 15 == 0) {
            player.playSound(SoundEvents.WARDEN_HEARTBEAT, 1.0F, 1.0F);
        }
    }
}
