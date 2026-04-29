package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Speed Relic. Gives the player constant Speed if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class SwiftnessRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Speed 2 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public SwiftnessRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.MOVEMENT_SPEED,
                40,
                "tooltip.potionrelicsmod.swiftnessrelic");
    }

    /**
     * Returns the Swiftness level from the configs.
     * 
     * @return The Swiftness level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.swiftnessLevel;
    }

    /**
     * Returns if the Swiftness Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.swiftnessUpgrade;
    }

    /**
     * Returns if the Swiftness Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.swiftnessParticles;
    }
}
