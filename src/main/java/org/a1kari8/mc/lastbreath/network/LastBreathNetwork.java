package org.a1kari8.mc.lastbreath.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.network.handler.DyingStateHandler;
import org.a1kari8.mc.lastbreath.network.handler.RescueStateHandler;
import org.a1kari8.mc.lastbreath.network.payload.DyingStatePayload;
import org.a1kari8.mc.lastbreath.network.payload.RescueStatePayload;

@EventBusSubscriber(modid = LastBreath.MOD_ID)
public class LastBreathNetwork {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1"); // 协议版本号

        registrar.playToClient(
                RescueStatePayload.TYPE,
                RescueStatePayload.CODEC,
                RescueStateHandler::handleDataOnMain
        );

        registrar.playToClient(
                DyingStatePayload.TYPE,
                DyingStatePayload.CODEC,
                DyingStateHandler::handleDataOnMain
        );
    }
}
