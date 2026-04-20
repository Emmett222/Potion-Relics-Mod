package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Dolphin's Grace Relic. Gives the player constant Dolphin's Grace if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-19-2026
 */
public class DolphinsGraceRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Dolphin's Grace 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public DolphinsGraceRelic(Properties pProperties) {
        super(pProperties, MobEffects.DOLPHINS_GRACE, "tooltip.potionrelicsmod.dolphinsgracerelic", 0, false, false);
    }
}
