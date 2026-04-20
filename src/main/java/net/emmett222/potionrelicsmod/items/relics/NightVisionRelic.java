package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;

/**
 * Night Vision Relic. Gives the player constant Night Vision if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-19-2026
 */
public class NightVisionRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Night Vision 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public NightVisionRelic(Properties pProperties) {
        super(pProperties, MobEffects.NIGHT_VISION, 280, "tooltip.potionrelicsmod.nightvisionrelic", 0, false, false);
    }    
}
