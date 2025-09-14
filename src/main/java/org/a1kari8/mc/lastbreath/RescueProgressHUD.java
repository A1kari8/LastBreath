package org.a1kari8.mc.lastbreath;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import static org.a1kari8.mc.lastbreath.LastBreath.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class RescueProgressHUD {
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        float progress;
        if (ClientRescueManager.shouldRender()) {
            progress = ClientRescueManager.getProgress();
        } else {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barWidth = 100;
        int barHeight = 8;
        int x = screenWidth / 2 - barWidth / 2;
        int y = screenHeight - 50; // 经验条上方

        GuiGraphics graphics = event.getGuiGraphics();

        // 背景条
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xFF555555);

        // 进度条
        int filled = (int) (barWidth * progress);
        graphics.fill(x, y, x + filled, y + barHeight, 0xFF00FF00);

        // 可选文字
        graphics.drawString(mc.font, "救援中...", x + 20, y - 10, 0xFFFFFF, false);
    }
}
