package org.a1kari8.mc.lastbreath.mixin;

import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.ServerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, net.neoforged.neoforge.common.extensions.ILivingEntityExtension  {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //    @ModifyVariable(
//            method = "travel",
//            at = @At("STORE"), // 注入点：变量赋值时
//            name = "f4"
//    )
//    private float modifyWaterSpeed(float value) {
//        if ((Object) this instanceof Player player) {
//            if (player.getData(LastBreath.DYING)) {
//                return 0.2f;
//            }
//        }
//        return value;
//    }
    @Inject(
            method = "canBeSeenAsEnemy",
            at = @At("RETURN"),
            cancellable = true
    )
    public void canBeSeenAsEnemy(CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity)(Object)this) instanceof Player player && !ServerConfig.DYING_CAN_BE_SEEN_AS_ENEMY.getAsBoolean()) {
            cir.setReturnValue(cir.getReturnValue() && !player.getData(LastBreath.DYING));
        }
    }
}
