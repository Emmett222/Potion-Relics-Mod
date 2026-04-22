package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Hero Of The Village Relic. Gives the player constant Dolphin's Grace if in the
 * inventory.
 * 
 * @author Emmett Grebe
 * @version 4-22-2026
 */
public class HeroOfTheVillageRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Dolphin's Grace 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public HeroOfTheVillageRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.HERO_OF_THE_VILLAGE,
                70,
                "tooltip.potionrelicsmod.heroofthevillagerelic");
    }

    /**
     * Returns the Dolphin's Grace level from the configs.
     * 
     * @return The Dolphin's Grace level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.heroOfTheVillageLevel;
    }

    /**
     * Returns if the Dolphin's Grace Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.heroOfTheVillageUpgrade;
    }

    /**
     * Returns if the Dolphin's Grace Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.heroOfTheVillageParticles;
    }
}
