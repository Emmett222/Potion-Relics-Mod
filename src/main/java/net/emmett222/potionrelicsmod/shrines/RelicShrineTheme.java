package net.emmett222.potionrelicsmod.shrines;

import net.emmett222.potionrelicsmod.items.ModItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public enum RelicShrineTheme implements StringRepresentable {
    ABSORPTION("absorption", ModItems.ABSORPTIONRELIC, Blocks.GOLD_BLOCK, Blocks.GLOWSTONE,
            ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.TOTEM_OF_UNDYING, 0.95f, false),
    DOLPHINS_GRACE("dolphins_grace", ModItems.DOLPHINSGRACERELIC, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN,
            ParticleTypes.DOLPHIN, ParticleTypes.SPLASH, 1.1f, true),
    DRAGON("dragon", ModItems.DRAGONRELIC, Blocks.END_STONE_BRICKS, Blocks.CRYING_OBSIDIAN,
            ParticleTypes.DRAGON_BREATH, ParticleTypes.PORTAL, 0.65f, false),
    FIRE_RESISTANCE("fire_resistance", ModItems.FIRERESISTANCERELIC, Blocks.MAGMA_BLOCK, Blocks.SHROOMLIGHT,
            ParticleTypes.FLAME, ParticleTypes.LAVA, 0.8f, false),
    HASTE("haste", ModItems.HASTERELIC, Blocks.COPPER_BLOCK, Blocks.GOLD_BLOCK,
            ParticleTypes.CRIT, ParticleTypes.ENCHANT, 1.25f, false),
    HERO_OF_THE_VILLAGE("hero_of_the_village", ModItems.HEROOFTHEVILLAGERELIC, Blocks.EMERALD_BLOCK,
            Blocks.CHISELED_STONE_BRICKS, ParticleTypes.HAPPY_VILLAGER, ParticleTypes.HAPPY_VILLAGER, 1.15f, false),
    INVISIBILITY("invisibility", ModItems.INVISIBILITYRELIC, Blocks.TINTED_GLASS, Blocks.AMETHYST_BLOCK,
            ParticleTypes.PORTAL, ParticleTypes.WITCH, 0.7f, false),
    JUMP_BOOST("jump_boost", ModItems.JUMPBOOSTRELIC, Blocks.MOSS_BLOCK, Blocks.SLIME_BLOCK,
            ParticleTypes.CLOUD, ParticleTypes.ITEM_SLIME, 1.2f, false),
    NIGHT_VISION("night_vision", ModItems.NIGHTVISIONRELIC, Blocks.END_STONE_BRICKS, Blocks.SEA_LANTERN,
            ParticleTypes.END_ROD, ParticleTypes.END_ROD, 0.9f, false),
    REGENERATION("regeneration", ModItems.REGENERATIONRELIC, Blocks.CHERRY_PLANKS, Blocks.AMETHYST_BLOCK,
            ParticleTypes.HEART, ParticleTypes.HEART, 1.05f, false),
    RESISTANCE("resistance", ModItems.RESISTANCERELIC, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.OBSIDIAN,
            ParticleTypes.ENCHANT, ParticleTypes.CRIT, 0.75f, false),
    SATURATION("saturation", ModItems.SATURATIONRELIC, Blocks.HAY_BLOCK, Blocks.MELON,
            ParticleTypes.COMPOSTER, ParticleTypes.COMPOSTER, 1.0f, false),
    SLOW_FALLING("slow_falling", ModItems.SLOWFALLINGRELIC, Blocks.SMOOTH_QUARTZ, Blocks.GLASS,
            ParticleTypes.CLOUD, ParticleTypes.END_ROD, 1.3f, false),
    STRENGTH("strength", ModItems.STRENGTHRELIC, Blocks.RED_NETHER_BRICKS, Blocks.NETHERITE_BLOCK,
            ParticleTypes.CRIT, ParticleTypes.CRIT, 0.65f, false),
    SWIFTNESS("swiftness", ModItems.SWIFTNESSRELIC, Blocks.BLUE_ICE, Blocks.LAPIS_BLOCK,
            ParticleTypes.ELECTRIC_SPARK, ParticleTypes.ELECTRIC_SPARK, 1.35f, false),
    WATER_BREATHING("water_breathing", ModItems.WATERBREATHINGRELIC, Blocks.DARK_PRISMARINE,
            Blocks.PRISMARINE, ParticleTypes.BUBBLE, ParticleTypes.BUBBLE_COLUMN_UP, 1.1f, true);

    private final String serializedName;
    private final RegistryObject<Item> relicItem;
    private final ParticleOptions ambientParticle;
    private final ParticleOptions burstParticle;
    private final Block foundationBlock;
    private final Block pillarBlock;
    private final float discoveryPitch;
    private final boolean allowsWaterPlacement;

    RelicShrineTheme(String serializedName, RegistryObject<Item> relicItem, Block foundationBlock, Block pillarBlock,
            ParticleOptions ambientParticle, ParticleOptions burstParticle, float discoveryPitch,
            boolean allowsWaterPlacement) {
        this.serializedName = serializedName;
        this.relicItem = relicItem;
        this.foundationBlock = foundationBlock;
        this.pillarBlock = pillarBlock;
        this.ambientParticle = ambientParticle;
        this.burstParticle = burstParticle;
        this.discoveryPitch = discoveryPitch;
        this.allowsWaterPlacement = allowsWaterPlacement;
    }

    public static RelicShrineTheme fromRelic(ItemStack relic) {
        for (RelicShrineTheme theme : values()) {
            if (relic.is(theme.relicItem.get())) {
                return theme;
            }
        }

        return ABSORPTION;
    }

    public BlockState foundationState() {
        return foundationBlock.defaultBlockState();
    }

    public BlockState pillarState() {
        return pillarBlock.defaultBlockState();
    }

    public Block foundationBlock() {
        return foundationBlock;
    }

    public Block pillarBlock() {
        return pillarBlock;
    }

    public ParticleOptions ambientParticle() {
        return ambientParticle;
    }

    public ParticleOptions burstParticle() {
        return burstParticle;
    }

    public float discoveryPitch() {
        return discoveryPitch;
    }

    public boolean allowsWaterPlacement() {
        return allowsWaterPlacement;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
