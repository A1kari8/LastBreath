package org.a1kari8.mc.lastbreath.network.handler;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.a1kari8.mc.lastbreath.client.ClientRescueManager;
import org.a1kari8.mc.lastbreath.network.payload.RescueStatePayload;

public class RescueStateHandler {
    public static void handleDataOnMain(final RescueStatePayload data, final IPayloadContext context) {
        switch (data.rescueState()) {
            case NONE -> ClientRescueManager.cancel(); // 没有救援
            case START -> ClientRescueManager.start();
            case COMPLETE -> ClientRescueManager.complete(); // 救援完成
            case CANCEL -> ClientRescueManager.cancel(); // 被取消
        }
    }
}
