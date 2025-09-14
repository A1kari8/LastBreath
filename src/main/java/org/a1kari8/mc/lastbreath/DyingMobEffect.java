package org.a1kari8.mc.lastbreath;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DyingMobEffect extends MobEffect {
    public DyingMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x880000);
    }
}
