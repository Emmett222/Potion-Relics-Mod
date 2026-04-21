package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Saturation Relic. Gives the player constant Saturation if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-20-2026
 */
public class SaturationRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Saturation 1 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public SaturationRelic(Properties pProperties) {
        super(pProperties, MobEffects.SATURATION, 70, "tooltip.potionrelicsmod.saturationrelic", 0, true, false);
    }
}

