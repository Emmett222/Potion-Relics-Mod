package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Haste Relic. Gives the player constant Haste if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-20-2026
 */
public class HasteRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Haste 2 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public HasteRelic(Properties pProperties) {
        super(pProperties, MobEffects.DIG_SPEED, 70, "tooltip.potionrelicsmod.hasterelic", 1, true, false);
    }
}
