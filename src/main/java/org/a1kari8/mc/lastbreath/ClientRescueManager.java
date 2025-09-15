package org.a1kari8.mc.lastbreath;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import org.a1kari8.mc.lastbreath.network.RescueState;


public class ClientRescueManager {
    private static long rescueTick = 0;
    private static RescueState state = RescueState.NONE;
    private static final long RESCUE_DURATION_TICK = Config.RESCUE_DURATION_MILLISECOND.getAsInt() / 20;

    public static void update(final RescueState state) {
        ClientRescueManager.state = state;
    }

    public static void clear() {
        if (Minecraft.getInstance().level != null) {
            rescueTick = Minecraft.getInstance().level.getGameTime();
        }
        state = RescueState.NONE;
    }

    public static void complete() {
        state = RescueState.COMPLETED;
        if (Minecraft.getInstance().level != null) {
            rescueTick = Minecraft.getInstance().level.getGameTime();
        }
    }

    public static void cancel() {
        if (Minecraft.getInstance().level != null) {
            rescueTick = Minecraft.getInstance().level.getGameTime();
        }
        state = RescueState.CANCELLED;
    }

    public static boolean shouldRender() {
        long elapsed = 0;
        if (Minecraft.getInstance().level != null) {
            elapsed = Minecraft.getInstance().level.getGameTime() - rescueTick;
        }
        return state == RescueState.RESCUING && elapsed != 0;
    }

    public static float getProgress() {
        long elapsed = 0;
        if (Minecraft.getInstance().level != null) {
            elapsed = Minecraft.getInstance().level.getGameTime() - rescueTick;
        }
        return Math.min(elapsed / (float) RESCUE_DURATION_TICK, 1f);
    }
}

