package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Absorption Relic. Gives the player constant Absorption if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class AbsorptionRelic extends BaseRelic {

    /**
     * Explicit constructor.
     * Sets the effect to Absorption 4 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public AbsorptionRelic(Properties pProperties) {
        super(pProperties, MobEffects.ABSORPTION, 0, "tooltip.potionrelicsmod.absorptionrelic");
    }

    /**
     * Called each tick as long as the relic is in the inventory.
     * Absorption is handled centrally in ModEvents so the relic can coexist cleanly
     * with vanilla absorption sources like golden apples.
     * 
     * @param pStack      The ItemStack to be used. Not used in the override.
     * @param pLevel      The pLevel to be used. Not used in the override.
     * @param pEntity     The pEntity to be used.
     * @param pSlotId     The location in inventory to be used. Not used in the
     *                    override.
     * @param pIsSelected If the item is selected or not. Not used in the override.
     */
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        // Managed in ModEvents.
    }

    /**
     * Returns Absorption level based off of hearts. Calculated by absorptionAmount
     * divided by 4.
     * 
     * @return Returns Absorption level based off of hearts.
     */
    @Override
    protected int getConfigAmplifier() {
        return (int) (ModConfigs.absorptionAmount / 4) - 1;
    }

    /**
     * Does nothing since this Relic does not use BaseRelic's inventoryTick()
     * method.
     * 
     * @return Always returns false.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return false;
    }

    /**
     * Does nothing since this Relic does not use BaseRelic's inventoryTick()
     * method.
     * 
     * @return Always returns false.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return false;
    }
}
