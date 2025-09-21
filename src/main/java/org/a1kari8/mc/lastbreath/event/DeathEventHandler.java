package org.a1kari8.mc.lastbreath.event;


import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

        String sourceMsgId = event.getSource().getMsgId();
        if (ServerConfig.DYING_TP_SAFE_POS.getAsBoolean()) {
            ServerLevel level = (ServerLevel) player.level();
            switch (sourceMsgId) {
                case "lava" -> {
                    BlockPos spawnPos = findNearestSafeBlock(level, player.blockPosition(), ServerConfig.DYING_TP_SAFE_POS_SEARCH_RADIUS.getAsInt());
                    if (spawnPos != null) {
                        player.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY() + 1, spawnPos.getZ() + 0.5);
                    }
                    player.clearFire();
                }
                case "onFire" -> player.clearFire();
                case "inFire" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, false));
                    player.clearFire();
                }
                case "outOfWorld" -> {
                    // 设置安全高度
                    int safeY = Math.max(level.getMinBuildHeight() + 2, -64 + 2);
                    BlockPos center = new BlockPos((int) player.getX(), safeY, (int) player.getZ());

                    // 生成 3x3 平台
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos platformPos = center.offset(dx, -1, dz);
                            level.setBlockAndUpdate(platformPos, Blocks.GLASS.defaultBlockState());
                        }
                    }

                    // 传送玩家到平台中心
                    player.teleportTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
                }
                case "flyIntoWall" -> {
                    player.stopFallFlying();
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, false, false));
                }
            }
            player.setDeltaMovement(Vec3.ZERO);
        }
    }

    private static BlockPos findNearestSafeBlock(ServerLevel level, BlockPos center, int maxRadius) {
        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        // 只检查当前半径的边界点（曼哈顿距离等于 radius）
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != radius) continue;

                        BlockPos checkPos = center.offset(dx, dy, dz);

                        if (isSafeBlock(level, checkPos)) {
                            BlockPos above = checkPos.above();

                            if (level.isEmptyBlock(above)) {
                                return checkPos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    private static boolean isSafeBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        // 排除岩浆、火焰、仙人掌等危险方块
        if (state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.CACTUS) || state.is(Blocks.SOUL_FIRE)) {
            return false;
        }

        // 方块必须坚固且不是液体或可替换
        boolean sturdy = state.isFaceSturdy(level, pos, Direction.UP);
        boolean notLiquid = state.getFluidState().isEmpty();
        boolean notReplaceable = !state.canBeReplaced();

        // 必须有空间
        BlockPos above = pos.above();
        boolean headClear = level.isEmptyBlock(above);

        return sturdy && notLiquid && notReplaceable && headClear;
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
        serverPlayer.removeEffect(MobEffects.DARKNESS);
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
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, MobEffectInstance.MAX_AMPLIFIER, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, MobEffectInstance.INFINITE_DURATION, MobEffectInstance.MIN_AMPLIFIER, false, false, false));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getData(LastBreath.DYING) && player.tickCount % 15 == 0) {
            player.playSound(SoundEvents.WARDEN_HEARTBEAT, 1.0F, 1.0F);
        }
    }
}
