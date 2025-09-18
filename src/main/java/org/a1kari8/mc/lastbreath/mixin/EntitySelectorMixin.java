package org.a1kari8.mc.lastbreath.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin {
    @Final
    @Shadow
    @Mutable
    public static Predicate<Entity> NO_CREATIVE_OR_SPECTATOR;

    static {
        NO_CREATIVE_OR_SPECTATOR = entity -> !(entity instanceof Player)
                || !entity.isSpectator() && !((Player)entity).isCreative() && !entity.getData(LastBreath.DYING);
    }
}
