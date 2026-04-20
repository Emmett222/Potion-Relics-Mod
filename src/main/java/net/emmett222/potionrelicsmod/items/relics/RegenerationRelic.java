package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Regeneration Relic. Gives the player constant Regeneration if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-20-2026
 */
public class RegenerationRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Regeneration 2 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public RegenerationRelic(Properties pProperties) {
        super(pProperties, MobEffects.REGENERATION, "tooltip.potionrelicsmod.regenerationrelic", 1, true, false);
    }
}

