package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Conduit Power relic. Gives the player constant Conduit Power if in the
 * inventory.
 * 
 * @author Emmett Grebe
 * @version 4-22-2026
 */
public class WaterBreathingRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Conduit Power 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public WaterBreathingRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.CONDUIT_POWER,
                40,
                "tooltip.potionrelicsmod.waterbreathingrelic");
    }

    /**
     * Returns the Conduit Power level from the configs.
     * 
     * @return The Conduit Power level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.waterBreathingLevel;
    }

    /**
     * Returns if the Conduit Power relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.waterBreathingUpgrade;
    }

    /**
     * Returns if the Conduit Power relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.waterBreathingParticles;
    }
}
