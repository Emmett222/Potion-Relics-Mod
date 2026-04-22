package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Strength Relic. Gives the player constant Strength if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class StrengthRelic extends BaseRelic {
    /**
     * Explicit constructor.
     * Sets the effect to Strength 2 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public StrengthRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.DAMAGE_BOOST,
                70,
                "tooltip.potionrelicsmod.strengthrelic");
    }

    /**
     * Returns the Strength level from the configs.
     * 
     * @return The Strength level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.strengthLevel;
    }

    /**
     * Returns if the Strength Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.strengthUpgrade;
    }

    /**
     * Returns if the Strength Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.strengthParticles;
    }
}
