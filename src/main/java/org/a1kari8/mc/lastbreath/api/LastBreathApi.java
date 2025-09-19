package org.a1kari8.mc.lastbreath.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.a1kari8.mc.lastbreath.LastBreath;
import org.a1kari8.mc.lastbreath.ServerConfig;
import org.a1kari8.mc.lastbreath.event.DeathEventHandler;
import org.a1kari8.mc.lastbreath.event.RescueEventHandler;

public class LastBreathApi {
    private LastBreathApi() {}

    public static boolean isDying(Player player) {
        return player.getData(LastBreath.DYING);
    }
    public static void setDying(Player player) {
        DeathEventHandler.setDying(player, (float) ServerConfig.DYING_HEALTH.getAsDouble());
    }
    public static void setDying(Player player, float dyingHealth) {
        DeathEventHandler.setDying(player, dyingHealth);
    }
    public static void rescuePlayer(ServerPlayer player) {
        RescueEventHandler.rescuePlayer(null,player, (float) ServerConfig.RESCUE_HEALTH.getAsDouble());
    }
    public static void rescuePlayer(ServerPlayer player, float healthAfterRescue) {
        RescueEventHandler.rescuePlayer(null ,player, healthAfterRescue);
    }
    public static void setBleeding(ServerPlayer player, boolean bleeding) {
        player.setData(LastBreath.BLEEDING, bleeding);
    }
}
