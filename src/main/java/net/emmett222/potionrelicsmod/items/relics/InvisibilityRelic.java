package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Invisibility Relic. Gives the player constant Invisibility if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-19-2026
 */
public class InvisibilityRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Invisibility 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public InvisibilityRelic(Properties pProperties) {
        super(pProperties, MobEffects.INVISIBILITY, 280, "tooltip.potionrelicsmod.invisibilityrelic", 0, false, true);
    }
}
