package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Slow Falling Relic. Gives the player constant Slow Falling if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class SlowFallingRelic extends BaseRelic {
    /**
     * Explicit constructor.
     * Sets the effect to Slow Falling 1 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public SlowFallingRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.SLOW_FALLING,
                70,
                "tooltip.potionrelicsmod.slowfallingrelic");
    }

    /**
     * Returns the Slow Falling level from the configs.
     * 
     * @return The Slow Falling level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.slowFallingLevel;
    }

    /**
     * Returns if the Slow Falling Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.slowFallingUpgrade;
    }

    /**
     * Returns if the Slow Falling Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.slowFallingParticles;
    }
}
