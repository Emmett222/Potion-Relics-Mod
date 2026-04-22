package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Fire Resistance Grace Relic. Gives the player constant Fire Resistance if in
 * the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class FireResistanceRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Fire Resistance 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public FireResistanceRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.FIRE_RESISTANCE,
                70,
                "tooltip.potionrelicsmod.fireresistancerelic",
                ModConfigs.fireResistanceLevel,
                ModConfigs.fireResistanceUpgrade,
                ModConfigs.fireResistanceParticles);
    }

    /**
     * Returns the Fire Resistance level from the configs.
     * 
     * @return The Fire Resistance level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.fireResistanceLevel;
    }

    /**
     * Returns if the Fire Resistance Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.fireResistanceUpgrade;
    }

    /**
     * Returns if the Fire Resistance Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.fireResistanceParticles;
    }
}
