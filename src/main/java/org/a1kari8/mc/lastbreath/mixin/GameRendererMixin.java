package org.a1kari8.mc.lastbreath.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getData(LastBreath.DYING)) {
            // 模拟降低饱和度：保留色彩但偏灰
            RenderSystem.setShaderColor(0.7F, 0.6F, 0.6F, 1.0F);
        }
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void resetColor(DeltaTracker deltaTracker, CallbackInfo ci) {
        // 恢复正常颜色，防止影响其他渲染
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}

