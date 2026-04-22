package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Water Breathing Relic. Gives the player constant Water Breathing if in the
 * inventory.
 * 
 * @author Emmett Grebe
 * @version 4-22-2026
 */
public class WaterBreathingRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Water Breathing 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public WaterBreathingRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.WATER_BREATHING,
                70,
                "tooltip.potionrelicsmod.waterbreathingrelic");
    }

    /**
     * Returns the Water Breathing level from the configs.
     * 
     * @return The Water Breathing level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.waterBreathingLevel;
    }

    /**
     * Returns if the Water Breathing Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.waterBreathingUpgrade;
    }

    /**
     * Returns if the Water Breathing Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.waterBreathingParticles;
    }
}
