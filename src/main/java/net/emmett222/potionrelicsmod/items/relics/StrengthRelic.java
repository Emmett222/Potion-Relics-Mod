package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Strength Relic. Gives the player constant Strength if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-19-2026
 */
public class StrengthRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Strength 2 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public StrengthRelic(Properties pProperties) {
        super(pProperties, MobEffects.DAMAGE_BOOST, "tooltip.potionrelicsmod.strengthrelic", 1, true);
    }
}
