package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Saturation Relic. Gives the player constant Saturation if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class SaturationRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Saturation 1 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public SaturationRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.SATURATION,
                40,
                "tooltip.potionrelicsmod.saturationrelic");
    }

    /**
     * Returns the Saturation level from the configs.
     * 
     * @return The Saturation level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.saturationLevel;
    }

    /**
     * Returns if the Saturation Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.saturationUpgrade;
    }

    /**
     * Returns if the Saturation Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.saturationParticles;
    }
}
