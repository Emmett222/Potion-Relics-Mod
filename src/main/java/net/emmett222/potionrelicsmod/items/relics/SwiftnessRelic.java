package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Speed Relic. Gives the player constant Speed if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-20-2026
 */
public class SwiftnessRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Speed 2 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public SwiftnessRelic(Properties pProperties) {
        super(pProperties, MobEffects.MOVEMENT_SPEED, 70, "tooltip.potionrelicsmod.swiftnessrelic", 1, true, false);
    }
}
