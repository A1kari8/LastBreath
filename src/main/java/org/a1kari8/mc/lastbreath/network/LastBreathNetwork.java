package org.a1kari8.mc.lastbreath.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.RescueProgressPayload;

@EventBusSubscriber(modid = LastBreath.MODID)
public class LastBreathNetwork {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1"); // 协议版本号

        registrar.playBidirectional(
                RescueProgressPayload.TYPE,
                RescueProgressPayload.CODEC,
                PayloadHandler::handleDataOnMain
        );
    }
}
