package org.a1kari8.mc.lastbreath;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RescueManager {
    private static final Map<UUID, Long> rescueStartTime = new HashMap<>();
    private static final Map<UUID, UUID> rescuerAndTarget = new HashMap<>();
    private static final Map<UUID, UUID> targetAndRescuer = new HashMap<>();
//    private static final Map<UUID, Float> lastSyncedProgress = new HashMap<>();
    private static final long RESCUE_DURATION_TICK = Config.RESCUE_DURATION_MILLISECOND.getAsInt() / 20;

//    public static boolean shouldSyncProgress(Player player, float currentProgress) {
//        UUID id = player.getUUID();
//        float last = lastSyncedProgress.getOrDefault(id, -1f);
//
//        // 如果进度变化超过阈值（例如 0.01），则需要同步
//        if (Math.abs(currentProgress - last) > 0.01f) {
//            lastSyncedProgress.put(id, currentProgress);
//            return true;
//        }
//
//        return false;
//    }

    public static void updateRescue(Player rescuer, Player target) {
        rescueStartTime.putIfAbsent(target.getUUID(), rescuer.level().getGameTime());
        targetAndRescuer.putIfAbsent(target.getUUID(), rescuer.getUUID());
        rescuerAndTarget.putIfAbsent(rescuer.getUUID(), target.getUUID());
    }

    public static ServerPlayer getTarget(ServerPlayer rescuer) {
        return Objects.requireNonNull(rescuer.server.getPlayerList().getPlayer(rescuerAndTarget.get(rescuer.getUUID())));

    }

    public static ServerPlayer getRescuer(ServerPlayer target) {
        return Objects.requireNonNull(target.server.getPlayerList().getPlayer(targetAndRescuer.get(target.getUUID())));
    }

    public static void cancelBeingRescued(Player target) {
        rescueStartTime.remove(target.getUUID());
        rescuerAndTarget.remove(targetAndRescuer.get(target.getUUID()));
        targetAndRescuer.remove(target.getUUID());
    }

    public static void cancelRescuing(Player rescuer) {
        rescueStartTime.remove(rescuerAndTarget.get(rescuer.getUUID()));
        targetAndRescuer.remove(rescuerAndTarget.get(rescuer.getUUID()));
        rescuerAndTarget.remove(rescuer.getUUID());
    }

    public static boolean isBeingRescued(Player target) {
        return rescueStartTime.containsKey(target.getUUID());
    }

    public static boolean isRescuing(Player rescuer) {
        return rescuerAndTarget.containsKey(rescuer.getUUID());
    }

    public static float getBeingRescuedProgress(Player target) {
        Long start = rescueStartTime.get(target.getUUID());
        if (start == null) return 0f;
        long elapsed = target.level().getGameTime() - start;
        return Math.min(elapsed / (float) RESCUE_DURATION_TICK, 1f);
    }

    public static float getRescuingProgress(Player rescuer) {
        Long start = rescueStartTime.get(rescuerAndTarget.get(rescuer.getUUID()));
        if (start == null) return 0f;
        long elapsed = rescuer.level().getGameTime() - start;
        return Math.min(elapsed / (float) RESCUE_DURATION_TICK, 1f);
    }

    public static boolean isRescuingComplete(Player rescuer) {
        return getRescuingProgress(rescuer) >= 1f;
    }

    public static boolean isBeingRescuedComplete(Player target) {
        return getBeingRescuedProgress(target) >= 1f;
    }
}
