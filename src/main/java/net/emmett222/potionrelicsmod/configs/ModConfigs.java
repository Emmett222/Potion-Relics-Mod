package net.emmett222.potionrelicsmod.configs;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configs for Potion Relics Mod.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class ModConfigs {
        // Builder
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        // Absorption
        public static final ForgeConfigSpec.ConfigValue<Integer> ABSORPTION_RELIC_COOLDOWN;
        public static final ForgeConfigSpec.ConfigValue<Long> ABSORPTION_AMOUNT;

        // Dolphin's Grace
        public static final ForgeConfigSpec.ConfigValue<Integer> DOLPHINS_GRACE_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> DOLPHINS_GRACE_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> DOLPHINS_GRACE_PARTICLES;

        // Fire Resistance
        public static final ForgeConfigSpec.ConfigValue<Integer> FIRE_RESISTANCE_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> FIRE_RESISTANCE_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> FIRE_RESISTANCE_PARTICLES;

        // Haste
        public static final ForgeConfigSpec.ConfigValue<Integer> HASTE_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> HASTE_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> HASTE_PARTICLES;

        // Haste
        public static final ForgeConfigSpec.ConfigValue<Integer> HERO_OF_THE_VILLAGE_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> HERO_OF_THE_VILLAGE_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> HERO_OF_THE_VILLAGE_PARTICLES;

        // Invisibility
        public static final ForgeConfigSpec.ConfigValue<Integer> INVISIBILITY_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> INVISIBILITY_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> INVISIBILITY_PARTICLES;

        // Invisibility
        public static final ForgeConfigSpec.ConfigValue<Integer> JUMP_BOOST_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> JUMP_BOOST_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> JUMP_BOOST_PARTICLES;

        // Night Vision
        public static final ForgeConfigSpec.ConfigValue<Integer> NIGHT_VISION_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> NIGHT_VISION_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> NIGHT_VISION_PARTICLES;

        // Regeneration
        public static final ForgeConfigSpec.ConfigValue<Integer> REGENERATION_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> REGENERATION_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> REGENERATION_PARTICLES;

        // Resistance
        public static final ForgeConfigSpec.ConfigValue<Integer> RESISTANCE_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> RESISTANCE_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> RESISTANCE_PARTICLES;

        // Saturation
        public static final ForgeConfigSpec.ConfigValue<Integer> SATURATION_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> SATURATION_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> SATURATION_PARTICLES;

        // Slow Falling
        public static final ForgeConfigSpec.ConfigValue<Integer> SLOW_FALLING_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> SLOW_FALLING_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> SLOW_FALLING_PARTICLES;

        // Strength
        public static final ForgeConfigSpec.ConfigValue<Integer> STRENGTH_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> STRENGTH_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> STRENGTH_PARTICLES;

        // Swiftness
        public static final ForgeConfigSpec.ConfigValue<Integer> SWIFTNESS_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> SWIFTNESS_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> SWIFTNESS_PARTICLES;

        // Water Breathing
        public static final ForgeConfigSpec.ConfigValue<Integer> WATER_BREATHING_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<Boolean> WATER_BREATHING_UPGRADE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> WATER_BREATHING_PARTICLES;

        // Cache
        // --- STATIC CACHE ---
        public static float absorptionAmount;

        public static int absorptionCooldown, dolphinsGraceLevel, fireResistanceLevel, hasteLevel,
                        heroOfTheVillageLevel, invisibilityLevel, jumpBoostLevel,
                        nightVisionLevel, regenerationLevel, resistanceLevel, saturationLevel, slowFallingLevel, strengthLevel,
                        swiftnessLevel, waterBreathingLevel;

        public static boolean absorptionUpgrade, dolphinsGraceUpgrade, fireResistanceUpgrade, hasteUpgrade,
                        heroOfTheVillageUpgrade,
                        invisibilityUpgrade, jumpBoostUpgrade, nightVisionUpgrade, regenerationUpgrade, resistanceUpgrade,
                        saturationUpgrade, slowFallingUpgrade,
                        strengthUpgrade, swiftnessUpgrade, waterBreathingUpgrade;

        public static boolean absorptionParticles, dolphinsGraceParticles, fireResistanceParticles, hasteParticles,
                        heroOfTheVillageParticles,
                        invisibilityParticles, jumpBoostParticles, nightVisionParticles, regenerationParticles, resistanceParticles,
                        saturationParticles, slowFallingParticles, strengthParticles, swiftnessParticles, waterBreathingParticles;

        // Strings
        private static final String levelExplanation = " (0 = Level I, 1 = Level II, 2 = Level III, etc.)";

        /**
         * Static initializer. Runs exactly once.
         */
        static {
                // --- ABSORPTION ---
                BUILDER.push("Absorption");
                ABSORPTION_RELIC_COOLDOWN = BUILDER
                                .comment("How long the absorption cooldown lasts in ticks (20 ticks = 1 second)")
                                .defineInRange("relicCooldown", 1200, 0, 72000);
                ABSORPTION_AMOUNT = BUILDER
                                .comment("How many absorption points (half-hearts) the relic gives")
                                .defineInRange("absorptionAmount", 16L, 0L, 2048L);
                BUILDER.pop();

                // --- DOLPHIN'S GRACE ---
                BUILDER.push("Dolphin's Grace Relic");
                DOLPHINS_GRACE_LEVEL = BUILDER.comment("Amplifier level for Dolphin's Grace" + levelExplanation)
                                .defineInRange("dolphinsGraceLevel", 0, 0, 254);
                DOLPHINS_GRACE_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("dolphinsGraceUpgrade", true);
                DOLPHINS_GRACE_PARTICLES = BUILDER.comment("Show potion swirls?").define("dolphinsGraceParticles",
                                false);
                BUILDER.pop();

                // --- FIRE RESISTANCE ---
                BUILDER.push("Fire Resistance Relic");
                FIRE_RESISTANCE_LEVEL = BUILDER.comment("Amplifier level for Fire Resistance" + levelExplanation)
                                .defineInRange("fireResistanceLevel", 0, 0, 254);
                FIRE_RESISTANCE_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("fireResistanceUpgrade",
                                false);
                FIRE_RESISTANCE_PARTICLES = BUILDER.comment("Show potion swirls?").define("fireResistanceParticles",
                                false);
                BUILDER.pop();

                // --- HASTE ---
                BUILDER.push("Haste Relic");
                HASTE_LEVEL = BUILDER.comment("Amplifier level for Haste" + levelExplanation).defineInRange(
                                "hasteLevel", 1, 0,
                                254);
                HASTE_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("hasteUpgrade", true);
                HASTE_PARTICLES = BUILDER.comment("Show potion swirls?").define("hasteParticles", false);
                BUILDER.pop();

                // --- HERO OF THE VILLAGE ---
                BUILDER.push("Hero Of The Village Relic");
                HERO_OF_THE_VILLAGE_LEVEL = BUILDER.comment("Amplifier level for Hero Of The Village" + levelExplanation)
                                .defineInRange("heroOfTheVillageLevel", 1, 0, 254);
                HERO_OF_THE_VILLAGE_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("heroOfTheVillageUpgrade", true);
                HERO_OF_THE_VILLAGE_PARTICLES = BUILDER.comment("Show potion swirls?").define("heroOfTheVillageParticles", false);
                BUILDER.pop();

                // --- INVISIBILITY ---
                BUILDER.push("Invisibility Relic");
                INVISIBILITY_LEVEL = BUILDER.comment("Amplifier level for Invisibility" + levelExplanation)
                                .defineInRange("invisibilityLevel", 0, 0, 254);
                INVISIBILITY_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("invisibilityUpgrade", false);
                INVISIBILITY_PARTICLES = BUILDER.comment("Invisibility Relic shows particles").define(
                                "invisibilityParticles",
                                true);
                BUILDER.pop();

                // --- JUMP BOOST ---
                BUILDER.push("Jump Boost Relic");
                JUMP_BOOST_LEVEL = BUILDER.comment("Amplifier level for Jump Boost" + levelExplanation)
                                .defineInRange("jumpBoostLevel", 1, 0, 254);
                JUMP_BOOST_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("jumpBoostUpgrade", true);
                JUMP_BOOST_PARTICLES = BUILDER.comment("Show potion swirls?").define("jumpBoostParticles", false);
                BUILDER.pop();

                // --- NIGHT VISION ---
                BUILDER.push("Night Vision Relic");
                NIGHT_VISION_LEVEL = BUILDER.comment("Amplifier level for Night Vision" + levelExplanation)
                                .defineInRange("nightVisionLevel", 0, 0, 254);
                NIGHT_VISION_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("nightVisionUpgrade", false);
                NIGHT_VISION_PARTICLES = BUILDER.comment("Show potion swirls?").define("nightVisionParticles", false);
                BUILDER.pop();

                // --- REGENERATION ---
                BUILDER.push("Regeneration Relic");
                REGENERATION_LEVEL = BUILDER.comment("Amplifier level for Regeneration" + levelExplanation)
                                .defineInRange("regenerationLevel", 0, 0, 254);
                REGENERATION_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("regenerationUpgrade", true);
                REGENERATION_PARTICLES = BUILDER.comment("Show potion swirls?").define("regenerationParticles", false);
                BUILDER.pop();

                // --- RESISTANCE ---
                BUILDER.push("Resistance Relic");
                RESISTANCE_LEVEL = BUILDER.comment("Amplifier level for Resistance" + levelExplanation)
                                .defineInRange("resistanceLevel", 0, 0, 254);
                RESISTANCE_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("resistanceUpgrade", true);
                RESISTANCE_PARTICLES = BUILDER.comment("Show potion swirls?").define("resistanceParticles", false);
                BUILDER.pop();

                // --- SATURATION ---
                BUILDER.push("Saturation Relic");
                SATURATION_LEVEL = BUILDER.comment("Amplifier level for Saturation" + levelExplanation)
                                .defineInRange("saturationLevel", 0, 0, 254);
                SATURATION_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("saturationUpgrade", true);
                SATURATION_PARTICLES = BUILDER.comment("Show potion swirls?").define("saturationParticles", false);
                BUILDER.pop();

                // --- SLOW FALLING ---
                BUILDER.push("Slow Falling Relic");
                SLOW_FALLING_LEVEL = BUILDER.comment("Amplifier level for Slow Falling" + levelExplanation)
                                .defineInRange("slowFallingLevel", 0, 0, 254);
                SLOW_FALLING_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("slowFallingUpgrade", true);
                SLOW_FALLING_PARTICLES = BUILDER.comment("Show potion swirls?").define("slowFallingParticles", false);
                BUILDER.pop();

                // --- STRENGTH ---
                BUILDER.push("Strength Relic");
                STRENGTH_LEVEL = BUILDER.comment("Amplifier level for Strength" + levelExplanation)
                                .defineInRange("strengthLevel", 1, 0, 254);
                STRENGTH_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("strengthUpgrade", true);
                STRENGTH_PARTICLES = BUILDER.comment("Show potion swirls?").define("strengthParticles", false);
                BUILDER.pop();

                // --- SWIFTNESS ---
                BUILDER.push("Swiftness Relic");
                SWIFTNESS_LEVEL = BUILDER.comment("Amplifier level for Swiftness" + levelExplanation)
                                .defineInRange("swiftnessLevel", 1, 0, 254);
                SWIFTNESS_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("swiftnessUpgrade", true);
                SWIFTNESS_PARTICLES = BUILDER.comment("Show potion swirls?").define("swiftnessParticles", false);
                BUILDER.pop();

                // --- SWIFTNESS ---
                BUILDER.push("Water Breathing Relic");
                WATER_BREATHING_LEVEL = BUILDER.comment("Amplifier level for Water Breathing" + levelExplanation)
                                .defineInRange("waterBreathingLevel", 0, 0, 254);
                WATER_BREATHING_UPGRADE = BUILDER.comment("Can upgrade in offhand").define("waterBreathingUpgrade",
                                false);
                WATER_BREATHING_PARTICLES = BUILDER.comment("Show potion swirls?").define("waterBreathingParticles",
                                false);
                BUILDER.pop();

                SPEC = BUILDER.build();
        }

        /**
         * Takes the configs and puts them in static variables. Much faster than calling
         * this method every time a config is needed.
         * Here we also make the minimum be 0 for number configs to prevent crashing if
         * the user puts a negative for the level.
         */
        public static void cacheConfigs() {
                // Absorption
                absorptionAmount = Math.max(0, ABSORPTION_AMOUNT.get());
                absorptionCooldown = Math.max(0, ABSORPTION_RELIC_COOLDOWN.get());

                // Dolphin's Grace
                dolphinsGraceLevel = Math.max(0, DOLPHINS_GRACE_LEVEL.get());
                dolphinsGraceUpgrade = DOLPHINS_GRACE_UPGRADE.get();
                dolphinsGraceParticles = DOLPHINS_GRACE_PARTICLES.get();

                // Fire Resistance
                fireResistanceLevel = Math.max(0, FIRE_RESISTANCE_LEVEL.get());
                fireResistanceUpgrade = FIRE_RESISTANCE_UPGRADE.get();
                fireResistanceParticles = FIRE_RESISTANCE_PARTICLES.get();

                // Haste
                hasteLevel = Math.max(0, HASTE_LEVEL.get());
                hasteUpgrade = HASTE_UPGRADE.get();
                hasteParticles = HASTE_PARTICLES.get();

                // Hero Of The Village
                heroOfTheVillageLevel = Math.max(0, HERO_OF_THE_VILLAGE_LEVEL.get());
                heroOfTheVillageUpgrade = HERO_OF_THE_VILLAGE_UPGRADE.get();
                heroOfTheVillageParticles = HERO_OF_THE_VILLAGE_PARTICLES.get();

                // Invisibility
                invisibilityLevel = Math.max(0, INVISIBILITY_LEVEL.get());
                invisibilityUpgrade = INVISIBILITY_UPGRADE.get();
                invisibilityParticles = INVISIBILITY_PARTICLES.get();

                // Jump Boost
                jumpBoostLevel = Math.max(0, JUMP_BOOST_LEVEL.get());
                jumpBoostUpgrade = JUMP_BOOST_UPGRADE.get();
                jumpBoostParticles = JUMP_BOOST_PARTICLES.get();

                // Night Vision
                nightVisionLevel = Math.max(0, NIGHT_VISION_LEVEL.get());
                nightVisionUpgrade = NIGHT_VISION_UPGRADE.get();
                nightVisionParticles = NIGHT_VISION_PARTICLES.get();

                // Regeneration
                regenerationLevel = Math.max(0, REGENERATION_LEVEL.get());
                regenerationUpgrade = REGENERATION_UPGRADE.get();
                regenerationParticles = REGENERATION_PARTICLES.get();

                // Resistance
                resistanceLevel = Math.max(0, RESISTANCE_LEVEL.get());
                resistanceUpgrade = RESISTANCE_UPGRADE.get();
                resistanceParticles = RESISTANCE_PARTICLES.get();

                // Saturation
                saturationLevel = Math.max(0, SATURATION_LEVEL.get());
                saturationUpgrade = SATURATION_UPGRADE.get();
                saturationParticles = SATURATION_PARTICLES.get();

                // Slow Falling
                slowFallingLevel = Math.max(0, SLOW_FALLING_LEVEL.get());
                slowFallingUpgrade = SLOW_FALLING_UPGRADE.get();
                slowFallingParticles = SLOW_FALLING_PARTICLES.get();

                // Strength
                strengthLevel = Math.max(0, STRENGTH_LEVEL.get());
                strengthUpgrade = STRENGTH_UPGRADE.get();
                strengthParticles = STRENGTH_PARTICLES.get();

                // Swiftness
                swiftnessLevel = Math.max(0, SWIFTNESS_LEVEL.get());
                swiftnessUpgrade = SWIFTNESS_UPGRADE.get();
                swiftnessParticles = SWIFTNESS_PARTICLES.get();

                // Water Breathing
                waterBreathingLevel = Math.max(0, WATER_BREATHING_LEVEL.get());
                waterBreathingUpgrade = WATER_BREATHING_UPGRADE.get();
                waterBreathingParticles = WATER_BREATHING_PARTICLES.get();
        }
}
