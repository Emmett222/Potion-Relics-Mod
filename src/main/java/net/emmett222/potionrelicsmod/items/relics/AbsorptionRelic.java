package net.emmett222.potionrelicsmod.items.relics;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Absorption Relic. Gives the player constant Absorption if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-20-2026
 */
public class AbsorptionRelic extends BaseRelic{

    /**
     * Explicit constructor.
     * Sets the effect to Absorption 4 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public AbsorptionRelic(Properties pProperties) {
        super(pProperties, MobEffects.ABSORPTION, "tooltip.potionrelicsmod.absorptionrelic", 3, false, false);
    }

    /**
     * Called each tick as long as the relic is in the inventory.
     * Gives the effect, if the item holder is a player of course.
     * Absorption has a special inventoryTick because the normal one with Absorption makes the player invincible.
     * 
     * @param pStack The ItemStack to be used. Not used in the override.
     * @param pLevel The pLevel to be used. Not used in the override.
     * @param pEntity The pEntity to be used.
     * @param pSlotId The location in inventory to be used. Not used in the override.
     * @param pIsSelected If the item is selected or not. Not used in the override.
     */
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        // If the entity is not a player, do nothing to it.
        if (pEntity.getType() != EntityType.PLAYER) {
            return;
        }
        if (pEntity instanceof Player living) {
            if ((living.getAbsorptionAmount() == 0.0) && !living.getCooldowns().isOnCooldown(this)) {
                living.setAbsorptionAmount(16.0f);
                living.getCooldowns().addCooldown(this, 1200);
            }
        }
    }
}
