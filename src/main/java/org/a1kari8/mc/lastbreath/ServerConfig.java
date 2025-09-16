package org.a1kari8.mc.lastbreath;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue RESCUE_DURATION_MILLISECOND = BUILDER
            .comment("Duration in millisecond for the rescue action")
            .defineInRange("rescueDuration", 3000, 1, Short.MAX_VALUE);

    // 该条必须放在最后
    public static final ModConfigSpec SPEC = BUILDER.build();

//    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
//            .comment("Whether to log the dirt block on common setup")
//            .define("logDirtBlock", true);
//
//    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
//            .comment("A magic number")
//            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);
//
//    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
//            .comment("What you want the introduction message to be for the magic number")
//            .define("magicNumberIntroduction", "The magic number is... ");
//
//    // a list of strings that are treated as resource locations for items
//    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
//            .comment("A list of items to log on common setup.")
//            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);
//
//    // LastBreath mod configuration
//    public static final ModConfigSpec.IntValue DYING_TIME = BUILDER
//            .comment("Time in seconds a player stays in dying state before actually dying")
//            .defineInRange("dyingTime", 60, 10, 600);
//
//    public static final ModConfigSpec.IntValue REVIVE_TIME = BUILDER
//            .comment("Time in seconds needed to revive a dying player")
//            .defineInRange("reviveTime", 5, 1, 30);
//
//    public static final ModConfigSpec.DoubleValue REVIVE_DISTANCE = BUILDER
//            .comment("Maximum distance for reviving a dying player")
//            .defineInRange("reviveDistance", 3.0, 1.0, 10.0);
//
//    public static final ModConfigSpec.BooleanValue ENABLE_DYING_EFFECTS = BUILDER
//            .comment("Enable negative effects while in dying state")
//            .define("enableDyingEffects", true);
//
//    public static final ModConfigSpec.BooleanValue BROADCAST_DYING_MESSAGE = BUILDER
//            .comment("Broadcast message to all players when someone enters dying state")
//            .define("broadcastDyingMessage", true);
//
//    public static final ModConfigSpec.DoubleValue HEALTH_AFTER_REVIVE = BUILDER
//            .comment("Health percentage restored after being revived (0.0 to 1.0)")
//            .defineInRange("healthAfterRevive", 0.5, 0.1, 1.0);


//    private static boolean validateItemName(final Object obj) {
//        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
//    }
}
