package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Invisibility Relic. Gives the player constant Invisibility if in the
 * inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class InvisibilityRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Invisibility 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public InvisibilityRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.INVISIBILITY,
                280,
                "tooltip.potionrelicsmod.invisibilityrelic");
    }

    /**
     * Returns the Invisibility level from the configs.
     * 
     * @return The Invisibility level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.invisibilityLevel;
    }

    /**
     * Returns if the Invisibility Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.invisibilityUpgrade;
    }

    /**
     * Returns if the Invisibility Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.invisibilityParticles;
    }
}
