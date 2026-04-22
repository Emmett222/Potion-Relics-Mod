package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Jump Boost Relic. Gives the player constant Jump Boost if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class JumpBoostRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Jump Boost 2 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public JumpBoostRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.JUMP,
                70,
                "tooltip.potionrelicsmod.jumpboostrelic");
    }

    /**
     * Returns the Jump Boost level from the configs.
     * 
     * @return The Jump Boost level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.jumpBoostLevel;
    }

    /**
     * Returns if the Jump Boost Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.jumpBoostUpgrade;
    }

    /**
     * Returns if the Jump Boost Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.jumpBoostParticles;
    }
}
