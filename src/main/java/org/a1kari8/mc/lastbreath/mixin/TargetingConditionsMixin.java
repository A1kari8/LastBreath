package org.a1kari8.mc.lastbreath.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetingConditions.class)
public class TargetingConditionsMixin {
    @Inject(
            method = "test",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onTest(LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof Player player) {
            if (player.getPersistentData().getBoolean("Dying")) {
                cir.setReturnValue(false); // 阻止目标成立
            }
        }
    }
}
