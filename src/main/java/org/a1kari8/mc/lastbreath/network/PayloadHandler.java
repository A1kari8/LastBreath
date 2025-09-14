package org.a1kari8.mc.lastbreath.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.a1kari8.mc.lastbreath.ClientRescueManager;
import org.a1kari8.mc.lastbreath.RescueProgressPayload;

public class PayloadHandler {
    public static void handleDataOnMain(final RescueProgressPayload data, final IPayloadContext context) {
        // Do something with the data, on the main thread
        switch (data.rescueState()) {
            case NONE -> {
                ClientRescueManager.clear();
                // 没有救援
            }
            case RESCUING -> {
                // 正在救援
                ClientRescueManager.update(data.progress(), data.rescueState());
            }
            case COMPLETED -> {
                // 救援完成
                ClientRescueManager.update(data.progress(), data.rescueState());
            }
            case CANCELLED -> {
                // 被取消
                ClientRescueManager.update(data.progress(), data.rescueState());
            }
        }
    }
}
