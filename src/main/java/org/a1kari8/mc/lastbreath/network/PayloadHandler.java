package org.a1kari8.mc.lastbreath.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.a1kari8.mc.lastbreath.client.ClientRescueManager;

public class PayloadHandler {
    public static void handleDataOnMain(final RescueProgressPayload data, final IPayloadContext context) {
        // Do something with the data, on the main thread
        switch (data.rescueState()) {
            case NONE -> {
                ClientRescueManager.cancel();
                // 没有救援
            }
            case START -> {
                ClientRescueManager.start();
            }
            case RESCUING -> {
                // 正在救援
//                ClientRescueManager.update(data.rescueState());
            }
            case COMPLETE -> {
                // 救援完成
                ClientRescueManager.complete();
            }
            case CANCEL -> {
                // 被取消
                ClientRescueManager.cancel();
            }
        }
    }
}
