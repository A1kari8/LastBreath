package org.a1kari8.mc.lastbreath.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import org.a1kari8.mc.lastbreath.LastBreath;

@EventBusSubscriber(modid = LastBreath.MOD_ID)
public class PlayerDriveEventHandler {
    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (!(event.getEntityMounting() instanceof ServerPlayer player)) return;
        if (!(event.getEntityBeingMounted() instanceof Boat boat)) return;

        if (player.getData(LastBreath.DYING)) {
            if (boat.getPassengers().isEmpty()) {
                event.setCanceled(true); // 阻止濒死玩家成为第一个乘客
                player.sendSystemMessage(Component.literal("濒死状态下无法驾驶船"));
            }
        }
    }
}
