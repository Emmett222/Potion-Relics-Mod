package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Dolphin's Grace Relic. Gives the player constant Dolphin's Grace if in the
 * inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class DolphinsGraceRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Dolphin's Grace 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public DolphinsGraceRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.DOLPHINS_GRACE,
                40,
                "tooltip.potionrelicsmod.dolphinsgracerelic");
    }

    /**
     * Returns the Dolphin's Grace level from the configs.
     * 
     * @return The Dolphin's Grace level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.dolphinsGraceLevel;
    }

    /**
     * Returns if the Dolphin's Grace Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.dolphinsGraceUpgrade;
    }

    /**
     * Returns if the Dolphin's Grace Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.dolphinsGraceParticles;
    }
}
