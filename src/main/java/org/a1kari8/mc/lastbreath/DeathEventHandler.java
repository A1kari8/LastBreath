package org.a1kari8.mc.lastbreath;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import static org.a1kari8.mc.lastbreath.LastBreath.MODID;

@EventBusSubscriber(modid = MODID)
public class DeathEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // 如果已经濒死过一次，就允许死亡
        if (player.getPersistentData().getBoolean("Dying")) return;

        // 阻止死亡
        event.setCanceled(true);

        // 设置濒死状态
        player.getPersistentData().putBoolean("Dying", true);
        player.setForcedPose(Pose.SLEEPING);
        player.setHealth(10.0F);
        player.addEffect(new MobEffectInstance(LastBreath.DYING_MOB_EFFECT, Integer.MAX_VALUE, 0, false, false));
    }
}
