package org.a1kari8.mc.lastbreath.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(AvoidEntityGoal.class)
public class AvoidEntityGoalMixin {
    @Shadow
    @Nullable
    protected LivingEntity toAvoid;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void cancelIfDowned(CallbackInfoReturnable<Boolean> cir) {
        if (toAvoid instanceof Player player) {
            System.out.println("AvoidEntityGoalMixin: call canUse, toAvoid is player");
            if (player.getPersistentData().getBoolean("Dying")) {
                System.out.println("AvoidEntityGoalMixin: Cancelling canUse because player is downed");
                toAvoid = null;
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void cancelIfDownedContinue(CallbackInfoReturnable<Boolean> cir) {
        if (toAvoid instanceof Player player) {
            System.out.println("AvoidEntityGoalMixin: call canContinueToUse, toAvoid is player");
            if (player.getPersistentData().getBoolean("Dying")) {
                System.out.println("AvoidEntityGoalMixin: Cancelling canContinueToUse because player is downed");
                toAvoid = null;
                cir.setReturnValue(false);
            }
        }
    }
}
