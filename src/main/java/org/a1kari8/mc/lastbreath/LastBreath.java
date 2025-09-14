package org.a1kari8.mc.lastbreath;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(LastBreath.MODID)
public class LastBreath {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "lastbreath";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "lastbreath" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab for the mod
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LASTBREATH_TAB = CREATIVE_MODE_TABS.register("lastbreath_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.lastbreath"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(Items.TOTEM_OF_UNDYING::getDefaultInstance)
            .displayItems((parameters, output) -> {
                // 可以在这里添加mod相关的物品
            }).build());

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final Holder<MobEffect> DYING_MOB_EFFECT = MOB_EFFECTS.register("dying_mob_effect", () -> new DyingMobEffect()
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(MODID, "dying_speed"), -0.9, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    public LastBreath(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        MOB_EFFECTS.register(modEventBus);

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("LastBreath mod setup complete - Dying state system initialized");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("Dying time configured: {} seconds", Config.DYING_TIME.get());
        LOGGER.info("Revive time configured: {} seconds", Config.REVIVE_TIME.get());
        LOGGER.info("Revive distance configured: {} blocks", Config.REVIVE_DISTANCE.get());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("LastBreath dying system is now active on server");
    }
}
