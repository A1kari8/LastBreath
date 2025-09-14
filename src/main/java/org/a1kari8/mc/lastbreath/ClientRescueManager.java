package org.a1kari8.mc.lastbreath;

import org.a1kari8.mc.lastbreath.network.RescueState;

public class ClientRescueManager {
    private static float progress = 0.0f;
    private static RescueState state = RescueState.NONE;

    public static void update(final float progress, final RescueState state) {
        ClientRescueManager.progress = progress;
        ClientRescueManager.state = state;
    }

    public static void clear() {
        progress = 0.0f;
        state = RescueState.NONE;
    }

    public static boolean shouldRender() {
        return state == RescueState.RESCUING;
    }

    public static float getProgress() {
        return progress;
    }
}

