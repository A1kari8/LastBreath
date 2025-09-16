package org.a1kari8.mc.lastbreath.client;

import net.minecraft.client.Minecraft;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.ServerRescueManager;
import org.a1kari8.mc.lastbreath.network.RescueState;

public class ClientRescueManager {
    private static long rescueStartTick = 0;
    private static RescueState state = RescueState.NONE;

    public static void start() {
        if (Minecraft.getInstance().level != null) {
            rescueStartTick = Minecraft.getInstance().level.getGameTime();
        }
        state = RescueState.RESCUING;
    }

    public static void complete() {
        state = RescueState.COMPLETE;
        if (Minecraft.getInstance().level != null) {
            rescueStartTick = Minecraft.getInstance().level.getGameTime();
        }
    }

    public static void cancel() {
        if (Minecraft.getInstance().level != null) {
            rescueStartTick = Minecraft.getInstance().level.getGameTime();
        }
        state = RescueState.CANCEL;
    }

    public static boolean shouldRender() {
        long elapsed = 0;
        if (Minecraft.getInstance().level != null) {
            elapsed = Minecraft.getInstance().level.getGameTime() - rescueStartTick;
        }
        return state == RescueState.RESCUING && elapsed != 0;
    }

    public static float getProgress() {
        if (state != RescueState.RESCUING) {
            return 0.0f;
        }
        long elapsed = 0;
        if (Minecraft.getInstance().level != null) {
            elapsed = Minecraft.getInstance().level.getGameTime() - rescueStartTick;
        }
        return Math.min(elapsed / (float) ServerRescueManager.getRescueDurationTick(), 1f);
    }

    public static float getLeftTimeSeconds() {
        if (state != RescueState.RESCUING) {
            return 0.0f;
        }
        long elapsed = 0;
        if (Minecraft.getInstance().level != null) {
            elapsed = Minecraft.getInstance().level.getGameTime() - rescueStartTick;
        }
        int leftTicks = ServerRescueManager.getRescueDurationTick() - (int) elapsed;
        return Math.max(leftTicks / 20f, 0f);
    }
}

