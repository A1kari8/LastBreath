package org.a1kari8.mc.lastbreath;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 管理玩家救援状态的工具类
 * 支持一对一救援关系，包括进度跟踪和状态管理
 */
public class ServerRescueManager {
    private static final Map<UUID, Long> rescueStartTick = new HashMap<>();
    private static final Map<UUID, Long> rescueCurrentTick = new HashMap<>();
    // 使用 Guava 双向字典维护一对一关系：rescuer <-> target
    private static final BiMap<UUID, UUID> rescuerAndTarget = HashBiMap.create();

    // 检测右键松开的阈值（tick）
    private static final int RELEASE_THRESHOLD_TICKS = 6;
//    private static long RESCUE_DURATION_TICK = 3000 / 20;

    public static int getRescueDurationTick() {
        return ServerConfig.RESCUE_DURATION_MILLISECOND.getAsInt() / 1000 * 20;
    }

    public static void startRescue(Player rescuer, Player target) {
        UUID targetId = target.getUUID();
        // 记录开始时间（以目标为 key）
        long gameTime = rescuer.level().getGameTime();
        rescueStartTick.put(targetId, gameTime);
        rescueCurrentTick.put(targetId, gameTime);
        rescuerAndTarget.put(rescuer.getUUID(), targetId);
    }


    public static void updateRescue(Player rescuer, Player target) {
        rescueCurrentTick.put(target.getUUID(), rescuer.level().getGameTime());
    }

    public static ServerPlayer getTarget(ServerPlayer rescuer) {
        return Objects.requireNonNull(rescuer.server.getPlayerList().getPlayer(rescuerAndTarget.get(rescuer.getUUID())));
    }

    public static ServerPlayer getRescuer(ServerPlayer target) {
        return Objects.requireNonNull(target.server.getPlayerList().getPlayer(rescuerAndTarget.inverse().get(target.getUUID())));
    }

    public static void cancelBeingRescued(Player target) {
        cancelBeingRescued(target.getUUID());

    }

    private static void cancelBeingRescued(UUID targetUUID) {
        rescueStartTick.remove(targetUUID);
        rescueCurrentTick.remove(targetUUID);
        rescuerAndTarget.inverse().remove(targetUUID);
    }

    public static void cancelRescuing(Player rescuer) {
        cancelBeingRescued(rescuerAndTarget.get(rescuer.getUUID()));
    }

    public static void completeRescuing(Player rescuer) {
        cancelRescuing(rescuer);
    }

    public static void completeBeingRescued(Player target) {
        cancelBeingRescued(target);
    }

    public static boolean isRightClickReleased(Player rescuer) {
        return rescuer.level().getGameTime() - rescueCurrentTick.getOrDefault(rescuerAndTarget.get(rescuer.getUUID()), 0L) > RELEASE_THRESHOLD_TICKS;
    }

    public static boolean isBeingRescued(Player target) {
        return rescuerAndTarget.inverse().containsKey(target.getUUID());
    }

    public static boolean isRescuing(Player rescuer) {
        return rescuerAndTarget.containsKey(rescuer.getUUID());
    }

    public static float getBeingRescuedProgress(Player target) {
        Long start = rescueStartTick.get(target.getUUID());
        if (start == null) return 0f;
        long elapsed = rescueCurrentTick.get(target.getUUID()) - start;
        return Math.min(elapsed / (float) getRescueDurationTick(), 1f);
    }

    public static float getRescuingProgress(Player rescuer) {
        Long start = rescueStartTick.get(rescuerAndTarget.get(rescuer.getUUID()));
        if (start == null) return 0f;
        long elapsed = rescueCurrentTick.get(rescuerAndTarget.get(rescuer.getUUID())) - start;
        return Math.min(elapsed / (float) getRescueDurationTick(), 1f);
    }

    public static boolean isRescuingComplete(Player rescuer) {
        return getRescuingProgress(rescuer) >= 1f;
    }

    public static boolean isBeingRescuedComplete(Player target) {
        return getBeingRescuedProgress(target) >= 1f;
    }
}

