package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Resistance Relic. Gives the player constant Resistance if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-19-2026
 */
public class ResistanceRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Resistance 1 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public ResistanceRelic(Properties pProperties) {
        super(pProperties, MobEffects.DAMAGE_RESISTANCE, "tooltip.potionrelicsmod.resistancerelic", 0, true, false);
    }
}
