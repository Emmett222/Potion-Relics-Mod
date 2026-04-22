package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Regeneration Relic. Gives the player constant Regeneration if in the
 * inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class RegenerationRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Regeneration 2 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public RegenerationRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.REGENERATION,
                70,
                "tooltip.potionrelicsmod.regenerationrelic");
    }

    /**
     * Returns the Regeneration level from the configs.
     * 
     * @return The Regeneration level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.regenerationLevel;
    }

    /**
     * Returns if the Regeneration Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.regenerationUpgrade;
    }

    /**
     * Returns if the Regeneration Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.regenerationParticles;
    }
}
