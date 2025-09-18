package org.a1kari8.mc.lastbreath.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.a1kari8.mc.lastbreath.LastBreath;

@EventBusSubscriber(modid = LastBreath.MOD_ID)
public class PlayerAttackEventHandler {
    /**
     * 拦截倒地玩家的攻击行为
     * @param event
     */
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (player.getData(LastBreath.DYING)) {
            event.setCanceled(true); // 阻止攻击
        }
    }

    /**
     * 拦截倒地玩家使用远程武器
     * @param event
     */
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().getData(LastBreath.DYING)) {
            ItemStack stack = event.getItemStack();
            if (isRangedWeapon(stack)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ProjectileWeaponItem || item instanceof TridentItem;
    }
}
