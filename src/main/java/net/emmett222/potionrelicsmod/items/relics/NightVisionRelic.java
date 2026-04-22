package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;

/**
 * Night Vision Relic. Gives the player constant Night Vision if in the
 * inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class NightVisionRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Night Vision 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public NightVisionRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.NIGHT_VISION,
                280,
                "tooltip.potionrelicsmod.nightvisionrelic",
                ModConfigs.nightVisionLevel,
                ModConfigs.nightVisionUpgrade,
                ModConfigs.nightVisionParticles);
    }

    /**
     * Returns the Night Vision level from the configs.
     * 
     * @return The Night Vision level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.nightVisionLevel;
    }

    /**
     * Returns if the Night Vision Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.nightVisionUpgrade;
    }

    /**
     * Returns if the Night Vision Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.nightVisionParticles;
    }
}
