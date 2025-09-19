package org.a1kari8.mc.lastbreath.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyVariable(
            method = "travel",
            at = @At("STORE"), // 注入点：变量赋值时
            name = "f4"
    )
    private float modifyWaterSpeed(float value) {
        if ((Object) this instanceof Player player) {
            if (player.getData(LastBreath.DYING)) {
                return 0.2f;
            }
        }
        return value;
    }
}
