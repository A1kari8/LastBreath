package org.a1kari8.mc.lastbreath.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.a1kari8.mc.lastbreath.LastBreath;

public class PlayerDamageEventHandler {
    @SubscribeEvent
    public static void onPlayerDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.getData(LastBreath.DYING)) return;
            DamageSource source = event.getSource();
            if ((source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_PROJECTILE) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)) && event.getSource().getEntity() instanceof Monster monster) {
                monster.setTarget(null);
                event.setCanceled(true);
            }
        }
    }
}
