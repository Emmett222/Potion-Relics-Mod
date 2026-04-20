package net.emmett222.potionrelicsmod.items.relics;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Night Vision Relic. Gives the player constant night vision if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-19-2026
 */
public class NightVisionRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Night Vision.
     * 
     * @param pProperties The pProperties to be used.
     */
    public NightVisionRelic(Properties pProperties) {
        super(pProperties, MobEffects.NIGHT_VISION, "tooltip.potionrelicsmod.nightvisionrelic", 0, false);
    }    
}
