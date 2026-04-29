package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Resistance Relic. Gives the player constant Resistance if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class ResistanceRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Resistance 1 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public ResistanceRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.DAMAGE_RESISTANCE,
                40,
                "tooltip.potionrelicsmod.resistancerelic");
    }

    /**
     * Returns the Resistance level from the configs.
     * 
     * @return The Resistance level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.resistanceLevel;
    }

    /**
     * Returns if the Resistance Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.resistanceUpgrade;
    }

    /**
     * Returns if the Resistance Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.resistanceParticles;
    }
}
