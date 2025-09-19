package org.a1kari8.mc.lastbreath.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import static org.a1kari8.mc.lastbreath.LastBreath.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
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

        int barWidth = 20;
        int barHeight = 2;
        int x = screenWidth / 2 - barWidth / 2;
        int y = screenHeight / 2 + 20; // 经验条上方

        GuiGraphics graphics = event.getGuiGraphics();

        // 背景条
        graphics.fill(x, y, x + barWidth, y + barHeight, 0x66A1A1A1);

        // 进度条
        int filled = (int) (barWidth * progress);
        graphics.fill(x, y, x + filled, y + barHeight, 0xAA15FF09);

        // 可选文字
        graphics.drawCenteredString(mc.font, String.format("%.2f", ClientRescueManager.getLeftTimeSeconds()), x + barWidth / 2, y - 10, 0xFFFFFF);
    }
}
