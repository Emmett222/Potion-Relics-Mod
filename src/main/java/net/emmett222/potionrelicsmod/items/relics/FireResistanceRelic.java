package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Fire Resistance Grace Relic. Gives the player constant Fire Resistance if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-20-2026
 */
public class FireResistanceRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Fire Resistance 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public FireResistanceRelic(Properties pProperties) {
        super(pProperties, MobEffects.FIRE_RESISTANCE, 40, "tooltip.potionrelicsmod.fireresistancerelic", 0, false, false);
    }
}
