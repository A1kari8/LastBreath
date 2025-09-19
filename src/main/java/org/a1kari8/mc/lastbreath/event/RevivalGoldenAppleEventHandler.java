package org.a1kari8.mc.lastbreath.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.a1kari8.mc.lastbreath.LastBreath;

import static org.a1kari8.mc.lastbreath.api.LastBreathApi.rescuePlayer;

@EventBusSubscriber(modid = LastBreath.MOD_ID)
public class RevivalGoldenAppleEventHandler {
    @SubscribeEvent
    public static void onItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItem();
        if ((stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) && player.getData(LastBreath.DYING)) {
            rescuePlayer(player);
            player.playSound(SoundEvents.TOTEM_USE, 1.0F, 1.0F);
        }
    }
}
