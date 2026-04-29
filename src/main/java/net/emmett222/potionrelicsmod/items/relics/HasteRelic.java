package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Haste Relic. Gives the player constant Haste if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class HasteRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Haste 2 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public HasteRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.DIG_SPEED,
                40,
                "tooltip.potionrelicsmod.hasterelic");
    }

    /**
     * Returns the Haste level from the configs.
     * 
     * @return The Haste level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.hasteLevel;
    }

    /**
     * Returns if the Haste Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.hasteUpgrade;
    }

    /**
     * Returns if the Haste Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.hasteParticles;
    }
}
